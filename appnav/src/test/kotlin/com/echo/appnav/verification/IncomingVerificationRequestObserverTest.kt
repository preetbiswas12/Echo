/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.appnav.verification

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.echo.appnav.session.MatrixSessionCache
import com.echo.appnav.session.SyncOrchestrator
import com.echo.features.networkmonitor.test.FakeNetworkMonitor
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.DeviceId
import com.echo.libraries.matrix.api.core.FlowId
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.matrix.api.verification.SessionVerificationRequestDetails
import com.echo.libraries.matrix.api.verification.VerificationRequest
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.matrix.test.A_SESSION_ID_2
import com.echo.libraries.matrix.test.A_USER_ID_3
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import com.echo.libraries.matrix.test.verification.FakeSessionVerificationService
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.services.appnavstate.test.FakeAppForegroundStateService
import com.echo.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class IncomingVerificationRequestObserverTest {
    private val sessionVerificationService1 = FakeSessionVerificationService()
    private val sessionVerificationService2 = FakeSessionVerificationService()

    @Test
    fun `incoming verification requests of all the sessions are emitted with their session id`() = runTest {
        val matrixSessionCache = createMatrixSessionCache()
        val observer = IncomingVerificationRequestObserver(
            matrixSessionCache = matrixSessionCache,
            appForegroundStateService = FakeAppForegroundStateService(),
        )
        observer.incomingVerificationRequests().test {
            matrixSessionCache.getOrRestore(A_SESSION_ID)
            matrixSessionCache.getOrRestore(A_SESSION_ID_2)
            runCurrent()
            val request = anIncomingVerificationRequest()
            sessionVerificationService2.listener!!.onIncomingSessionRequest(request)
            assertThat(awaitItem()).isEqualTo(IncomingVerificationRequestData(A_SESSION_ID_2, request))
            sessionVerificationService1.listener!!.onIncomingSessionRequest(request)
            assertThat(awaitItem()).isEqualTo(IncomingVerificationRequestData(A_SESSION_ID, request))
        }
        // The listeners are removed when the flow is not collected anymore
        assertThat(sessionVerificationService1.listener).isNull()
        assertThat(sessionVerificationService2.listener).isNull()
    }

    @Test
    fun `listener is removed when the session is removed from the cache`() = runTest {
        val matrixSessionCache = createMatrixSessionCache()
        val observer = IncomingVerificationRequestObserver(
            matrixSessionCache = matrixSessionCache,
            appForegroundStateService = FakeAppForegroundStateService(),
        )
        observer.incomingVerificationRequests().test {
            matrixSessionCache.getOrRestore(A_SESSION_ID)
            matrixSessionCache.getOrRestore(A_SESSION_ID_2)
            runCurrent()
            assertThat(sessionVerificationService1.listener).isNotNull()
            assertThat(sessionVerificationService2.listener).isNotNull()
            matrixSessionCache.remove(A_SESSION_ID)
            runCurrent()
            assertThat(sessionVerificationService1.listener).isNull()
            assertThat(sessionVerificationService2.listener).isNotNull()
        }
    }

    @Test
    fun `incoming user verification request of another session is emitted with its session id`() = runTest {
        val matrixSessionCache = createMatrixSessionCache()
        val observer = IncomingVerificationRequestObserver(
            matrixSessionCache = matrixSessionCache,
            appForegroundStateService = FakeAppForegroundStateService(),
        )
        observer.incomingVerificationRequests().test {
            matrixSessionCache.getOrRestore(A_SESSION_ID)
            matrixSessionCache.getOrRestore(A_SESSION_ID_2)
            runCurrent()
            val request = anIncomingUserVerificationRequest()
            sessionVerificationService2.listener!!.onIncomingSessionRequest(request)
            assertThat(awaitItem()).isEqualTo(IncomingVerificationRequestData(A_SESSION_ID_2, request))
        }
    }

    @Test
    fun `incoming verification request is emitted when the app goes to foreground`() = runTest {
        val matrixSessionCache = createMatrixSessionCache()
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false)
        val observer = IncomingVerificationRequestObserver(
            matrixSessionCache = matrixSessionCache,
            appForegroundStateService = appForegroundStateService,
        )
        observer.incomingVerificationRequests().test {
            matrixSessionCache.getOrRestore(A_SESSION_ID)
            runCurrent()
            val request = anIncomingVerificationRequest()
            sessionVerificationService1.listener!!.onIncomingSessionRequest(request)
            advanceTimeBy(30.seconds)
            expectNoEvents()
            appForegroundStateService.givenIsInForeground(true)
            assertThat(awaitItem()).isEqualTo(IncomingVerificationRequestData(A_SESSION_ID, request))
        }
    }

    @Test
    fun `incoming verification request is discarded if the app does not go to foreground in time`() = runTest {
        val matrixSessionCache = createMatrixSessionCache()
        val appForegroundStateService = FakeAppForegroundStateService(initialForegroundValue = false)
        val observer = IncomingVerificationRequestObserver(
            matrixSessionCache = matrixSessionCache,
            appForegroundStateService = appForegroundStateService,
        )
        observer.incomingVerificationRequests().test {
            matrixSessionCache.getOrRestore(A_SESSION_ID)
            runCurrent()
            sessionVerificationService1.listener!!.onIncomingSessionRequest(anIncomingVerificationRequest())
            advanceTimeBy(3.minutes)
            appForegroundStateService.givenIsInForeground(true)
            runCurrent()
            expectNoEvents()
        }
    }

    private fun TestScope.createMatrixSessionCache(): MatrixSessionCache {
        val matrixClient1 = FakeMatrixClient(
            sessionId = A_SESSION_ID,
            sessionCoroutineScope = backgroundScope,
            sessionVerificationService = sessionVerificationService1,
            userIdServerNameLambda = { "server" },
        )
        val matrixClient2 = FakeMatrixClient(
            sessionId = A_SESSION_ID_2,
            sessionCoroutineScope = backgroundScope,
            sessionVerificationService = sessionVerificationService2,
            userIdServerNameLambda = { "server" },
        )
        return MatrixSessionCache(
            authenticationService = FakeMatrixAuthenticationService(
                matrixClientResult = { sessionId ->
                    Result.success(if (sessionId == A_SESSION_ID) matrixClient1 else matrixClient2)
                },
            ),
            syncOrchestratorFactory = object : SyncOrchestrator.Factory {
                override fun create(matrixClient: MatrixClient, sessionCoroutineScope: CoroutineScope) = SyncOrchestrator(
                    matrixClient = matrixClient,
                    sessionCoroutineScope = sessionCoroutineScope,
                    appForegroundStateService = FakeAppForegroundStateService(),
                    networkMonitor = FakeNetworkMonitor(),
                    dispatchers = testCoroutineDispatchers(),
                    analyticsService = FakeAnalyticsService(),
                )
            },
            analyticsService = FakeAnalyticsService(),
        )
    }

    private fun anIncomingVerificationRequest() = VerificationRequest.Incoming.OtherSession(
        details = aSessionVerificationRequestDetails(senderProfile = MatrixUser(A_SESSION_ID)),
    )

    private fun anIncomingUserVerificationRequest() = VerificationRequest.Incoming.User(
        details = aSessionVerificationRequestDetails(senderProfile = MatrixUser(A_USER_ID_3)),
    )

    private fun aSessionVerificationRequestDetails(senderProfile: MatrixUser) = SessionVerificationRequestDetails(
        senderProfile = senderProfile,
        flowId = FlowId("flowId"),
        deviceId = DeviceId("deviceId"),
        deviceDisplayName = "a device name",
        firstSeenTimestamp = 0,
    )
}

