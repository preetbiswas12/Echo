/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import com.echo.features.enterprise.test.FakeClientBuilderEnterpriseHook
import com.echo.libraries.featureflag.test.FakeFeatureFlagService
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.paths.SessionPaths
import com.echo.libraries.matrix.api.scanner.ContentScanner
import com.echo.libraries.matrix.impl.auth.FakeProxyProvider
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiClientBuilder
import com.echo.libraries.matrix.impl.room.FakeTimelineEventFilterFactory
import com.echo.libraries.matrix.impl.search.RustSearchBackfillService
import com.echo.libraries.matrix.impl.search.workmanager.SearchBackfillRequestBuilder
import com.echo.libraries.matrix.impl.storage.FakeSqliteStoreBuilderProvider
import com.echo.libraries.matrix.impl.storage.SqliteStoreBuilderProvider
import com.echo.libraries.network.useragent.SimpleUserAgentProvider
import com.echo.libraries.sessionstorage.api.SessionStore
import com.echo.libraries.sessionstorage.test.InMemorySessionStore
import com.echo.libraries.sessionstorage.test.aSessionData
import com.echo.libraries.workmanager.api.WorkManagerRequestBuilder
import com.echo.libraries.workmanager.api.WorkManagerRequestWrapper
import com.echo.libraries.workmanager.test.FakeWorkManagerScheduler
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.services.toolbox.test.systemclock.FakeSystemClock
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import java.io.File
import org.matrix.rustcomponents.sdk.SyncService as ClientSyncService

@OptIn(ExperimentalCoroutinesApi::class)
class RustMatrixClientFactoryTest {
    @Test
    fun test() = runTest {
        val scheduleWorkersLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
        val workManagerScheduler = FakeWorkManagerScheduler(submitLambda = scheduleWorkersLambda, cancelLambda = { _, _ -> })
        val sut = createRustMatrixClientFactory(workManagerScheduler = workManagerScheduler)

        val result = sut.create(aSessionData())

        assertThat(result.sessionId).isEqualTo(SessionId("@alice:server.org"))
        runCurrent()
        // 2 workers are scheduled: one for the search backfill and one for the vacuum
        scheduleWorkersLambda.assertions().isCalledExactly(2)
        result.destroy()
    }

    @Test
    fun `an auth error on a client only invalidates its own session`() = runTest {
        val aliceSessionId = "@alice:server.org"
        val bobSessionId = "@bob:server.org"
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(sessionId = aliceSessionId, isTokenValid = true),
                aSessionData(sessionId = bobSessionId, isTokenValid = true),
            ),
            updateUserProfileResult = { _, _, _, _ -> },
        )
        val userIds = ArrayDeque(listOf(aliceSessionId, bobSessionId))
        val delegates = mutableMapOf<String, ClientDelegate?>()
        val sut = createRustMatrixClientFactory(
            sessionStore = sessionStore,
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}, cancelLambda = { _, _ -> }),
            clientBuilderProvider = FakeClientBuilderProvider {
                val userId = userIds.removeFirst()
                FakeFfiClientBuilder(
                    buildResult = {
                        FakeFfiClient(
                            userId = userId,
                            withUtdHook = {},
                            setDelegateResult = { delegates[userId] = it },
                        )
                    }
                )
            },
        )
        val aliceClient = sut.create(sessionStore.getSession(aliceSessionId)!!)
        val bobClient = sut.create(sessionStore.getSession(bobSessionId)!!)

        delegates.getValue(aliceSessionId)!!.didReceiveAuthError(isSoftLogout = false)

        assertThat(sessionStore.getSession(aliceSessionId)!!.isTokenValid).isFalse()
        assertThat(sessionStore.getSession(bobSessionId)!!.isTokenValid).isTrue()
        aliceClient.destroy()
        bobClient.destroy()
    }
}

fun TestScope.createRustMatrixClientFactory(
    cacheDirectory: File = File("/cache"),
    sessionStore: SessionStore = InMemorySessionStore(
        updateUserProfileResult = { _, _, _, _ -> },
    ),
    clientBuilderProvider: ClientBuilderProvider = FakeClientBuilderProvider(),
    workManagerScheduler: FakeWorkManagerScheduler = FakeWorkManagerScheduler(cancelLambda = { _, _ -> }),
    sqliteStoreBuilderProvider: SqliteStoreBuilderProvider = FakeSqliteStoreBuilderProvider(),
) = RustMatrixClientFactory(
    cacheDirectory = cacheDirectory,
    appCoroutineScope = backgroundScope,
    coroutineDispatchers = testCoroutineDispatchers(),
    sessionStore = sessionStore,
    userAgentProvider = SimpleUserAgentProvider(),
    proxyProvider = FakeProxyProvider(),
    analyticsService = FakeAnalyticsService(),
    featureFlagService = FakeFeatureFlagService(),
    clientBuilderProvider = clientBuilderProvider,
    sqliteStoreBuilderProvider = sqliteStoreBuilderProvider,
    clientBuilderEnterpriseHook = FakeClientBuilderEnterpriseHook(),
    innerMatrixClientFactory = createRustMatrixClientInnerFactory(sessionStore, workManagerScheduler),
)

private fun TestScope.createRustMatrixClientInnerFactory(
    sessionStore: SessionStore,
    workManagerScheduler: FakeWorkManagerScheduler,
) = object : RustMatrixClient.Factory {
    override fun create(
        sessionPaths: SessionPaths,
        innerClient: Client,
        innerSyncService: ClientSyncService,
        baseCacheDirectory: File,
        contentScanner: ContentScanner?,
        isMessageSearchAvailable: Boolean,
        sessionDelegate: RustClientSessionDelegate,
    ) = RustMatrixClient(
        sessionPaths = sessionPaths,
        innerClient = innerClient,
        innerSyncService = innerSyncService,
        baseCacheDirectory = baseCacheDirectory,
        contentScanner = contentScanner,
        isMessageSearchAvailable = isMessageSearchAvailable,
        sessionDelegate = sessionDelegate,
        sessionStore = sessionStore,
        appCoroutineScope = backgroundScope,
        dispatchers = testCoroutineDispatchers(),
        clock = FakeSystemClock(),
        timelineEventFilterFactory = FakeTimelineEventFilterFactory(),
        featureFlagService = FakeFeatureFlagService(),
        analyticsService = FakeAnalyticsService(),
        workManagerScheduler = workManagerScheduler,
        searchBackfillServiceFactory = object : RustSearchBackfillService.Factory {
            override fun create(innerClient: Client) = RustSearchBackfillService(
                innerClient = innerClient,
                searchBackfillRequestBuilderFactory = {
                    object : SearchBackfillRequestBuilder {
                        override suspend fun build(): Result<List<WorkManagerRequestWrapper>> = Result.success(emptyList())
                    }
                },
                workManagerScheduler = workManagerScheduler,
            )
        },
    )
}

