/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.appnav.session

import com.bumble.appyx.core.state.MutableSavedStateMapImpl
import com.echo.features.networkmonitor.test.FakeNetworkMonitor
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.services.appnavstate.test.FakeAppForegroundStateService
import com.echo.tests.testutils.testCoroutineDispatchers
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatrixSessionCacheTest {
    @Test
    fun `test getOrNull`() = runTest {
        val matrixSessionCache = createMatrixSessionCache()
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
    }

    @Test
    fun `test getSyncOrchestratorOrNull`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = createMatrixSessionCache(fakeAuthenticationService)

        // With no matrix client there is no sync orchestrator
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
        assertThat(matrixSessionCache.getSyncOrchestrator(A_SESSION_ID)).isNull()

        // But as soon as we receive a client, we can get the sync orchestrator
        val fakeMatrixClient = FakeMatrixClient(sessionCoroutineScope = backgroundScope, userIdServerNameLambda = { A_SESSION_ID.value })
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.getSyncOrchestrator(A_SESSION_ID)).isNotNull()
    }

    @Test
    fun `test getOrRestore`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = createMatrixSessionCache(fakeAuthenticationService)
        val fakeMatrixClient = FakeMatrixClient(sessionCoroutineScope = backgroundScope, userIdServerNameLambda = { A_SESSION_ID.value })
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        // Do it again to hit the cache
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
    }

    @Test
    fun `test remove`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = createMatrixSessionCache(fakeAuthenticationService)
        val fakeMatrixClient = FakeMatrixClient(sessionCoroutineScope = backgroundScope, userIdServerNameLambda = { A_SESSION_ID.value })
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.matrixClients.value).containsExactly(fakeMatrixClient)
        // Remove
        matrixSessionCache.remove(A_SESSION_ID)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
        assertThat(matrixSessionCache.matrixClients.value).isEmpty()
    }

    @Test
    fun `test remove all`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = createMatrixSessionCache(fakeAuthenticationService)
        val fakeMatrixClient = FakeMatrixClient(sessionCoroutineScope = backgroundScope, userIdServerNameLambda = { A_SESSION_ID.value })
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.matrixClients.value).containsExactly(fakeMatrixClient)
        // Remove all
        matrixSessionCache.removeAll()
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
        assertThat(matrixSessionCache.matrixClients.value).isEmpty()
    }

    @Test
    fun `test save and restore`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = createMatrixSessionCache(fakeAuthenticationService)
        val fakeMatrixClient = FakeMatrixClient(sessionCoroutineScope = backgroundScope, userIdServerNameLambda = { A_SESSION_ID.value })
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        matrixSessionCache.getOrRestore(A_SESSION_ID)
        val savedStateMap = MutableSavedStateMapImpl { true }
        matrixSessionCache.saveIntoSavedState(savedStateMap)
        assertThat(savedStateMap.size).isEqualTo(1)
        // Test Restore with non-empty map
        matrixSessionCache.restoreWithSavedState(savedStateMap)
        // Empty the map
        matrixSessionCache.removeAll()
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
        // Restore again
        matrixSessionCache.restoreWithSavedState(savedStateMap)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
    }

    @Test
    fun `test AuthenticationService listenToNewMatrixClients emits a Client value and we save it`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = createMatrixSessionCache(fakeAuthenticationService, createSyncOrchestratorFactory())
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()

        val loginSucceeded = fakeAuthenticationService.login("user", "pass")

        assertThat(loginSucceeded.isSuccess).isTrue()

        runCurrent()

        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNotNull()
    }

    private fun TestScope.createMatrixSessionCache(
        authenticationService: FakeMatrixAuthenticationService = FakeMatrixAuthenticationService(),
        syncOrchestratorFactory: SyncOrchestrator.Factory = createSyncOrchestratorFactory(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
    ) = MatrixSessionCache(
        authenticationService = authenticationService,
        syncOrchestratorFactory = syncOrchestratorFactory,
        analyticsService = analyticsService,
    )

    private fun TestScope.createSyncOrchestratorFactory(): SyncOrchestrator.Factory {
        val dispatchers = testCoroutineDispatchers()

        return object : SyncOrchestrator.Factory {
            override fun create(
                matrixClient: MatrixClient,
                sessionCoroutineScope: CoroutineScope,
            ): SyncOrchestrator {
                return SyncOrchestrator(
                    matrixClient = matrixClient,
                    sessionCoroutineScope = sessionCoroutineScope,
                    appForegroundStateService = FakeAppForegroundStateService(),
                    networkMonitor = FakeNetworkMonitor(),
                    dispatchers = dispatchers,
                    analyticsService = FakeAnalyticsService(),
                )
            }
        }
    }
}
