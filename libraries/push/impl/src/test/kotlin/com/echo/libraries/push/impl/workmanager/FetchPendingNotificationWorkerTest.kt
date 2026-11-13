/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.workmanager

import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.taskexecutor.WorkManagerTaskExecutor
import androidx.work.workDataOf
import com.echo.features.networkmonitor.api.NetworkStatus
import com.echo.features.networkmonitor.test.FakeNetworkMonitor
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.exception.ClientException
import com.echo.libraries.push.impl.db.PushRequest
import com.echo.libraries.push.impl.history.FakePushHistoryService
import com.echo.libraries.push.impl.notifications.FakeNotifiableEventResolver
import com.echo.libraries.push.impl.notifications.FakeNotificationResultProcessor
import com.echo.libraries.push.impl.notifications.fixtures.aPushRequest
import com.echo.libraries.push.impl.notifications.model.ResolvedPushEvent
import com.echo.libraries.push.impl.push.SyncOnNotifiableEvent
import com.echo.libraries.push.test.push.FakeFetchPushForegroundServiceManager
import com.echo.libraries.workmanager.api.WorkManagerRequestBuilder
import com.echo.libraries.workmanager.api.di.MetroWorkerFactory
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import com.echo.services.toolbox.test.systemclock.FakeSystemClock
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.robolectric.RobolectricTest
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class FetchPendingNotificationWorkerTest : RobolectricTest() {
    @Test
    fun `test - success`() = runTest {
        val callOrder = mutableListOf<String>()
        var synced = false
        val syncOnNotifiableEventLambda = SyncOnNotifiableEvent {
            synced = true
            callOrder += "sync"
        }
        val emitResultLambda = lambdaRecorder<Map<PushRequest, Result<ResolvedPushEvent>>, Unit> { callOrder += "emit" }
        val processor = FakeNotificationResultProcessor(emit = emitResultLambda)
        val stopForegroundServiceLambda = lambdaRecorder<Boolean> {
            callOrder += "stop"
            true
        }

        val getPendingResultsLambda = lambdaRecorder<SessionId, Instant?, Result<List<PushRequest>>> { _, _ -> Result.success(listOf(aPushRequest())) }
        val replacePushRequestsLambda = lambdaRecorder<List<PushRequest>, Result<Unit>> { Result.success(Unit) }
        val removeOldPushRequestsLambda = lambdaRecorder<SessionId, Result<Unit>> { Result.success(Unit) }
        val pushHistoryService = FakePushHistoryService(
            getPendingPushRequests = getPendingResultsLambda,
            replacePushRequests = replacePushRequestsLambda,
            removeOldPushRequests = removeOldPushRequestsLambda,
        )

        val worker = createWorker(
            input = "@alice:matrix.org",
            pushHistoryService = pushHistoryService,
            resultProcessor = processor,
            syncOnNotifiableEvent = syncOnNotifiableEventLambda,
            pushHandlingWakeLock = FakeFetchPushForegroundServiceManager(unlock = stopForegroundServiceLambda),
        )

        val result = worker.doWork()

        // The expected data is fetched and replaced from the service
        getPendingResultsLambda.assertions().isCalledOnce()
        replacePushRequestsLambda.assertions().isCalledOnce()
        removeOldPushRequestsLambda.assertions().isCalledOnce()

        // The process finished successfully
        assertThat(result).isEqualTo(ListenableWorker.Result.success())

        // A result was emitted
        emitResultLambda.assertions().isCalledOnce()

        // An opportunistic sync was triggered
        assertThat(synced).isTrue()

        // The foreground service is released once, after the results were handed over and before the opportunistic sync
        stopForegroundServiceLambda.assertions().isCalledOnce()
        assertThat(callOrder).containsExactly("emit", "stop", "sync").inOrder()
    }

    @Test
    fun `test - before Android 12 the foreground service is released as soon as the work runs`() = runTest {
        val callOrder = mutableListOf<String>()
        val syncOnNotifiableEventLambda = SyncOnNotifiableEvent { callOrder += "sync" }
        val emitResultLambda = lambdaRecorder<Map<PushRequest, Result<ResolvedPushEvent>>, Unit> { callOrder += "emit" }
        val processor = FakeNotificationResultProcessor(emit = emitResultLambda)
        val stopForegroundServiceLambda = lambdaRecorder<Boolean> {
            callOrder += "stop"
            true
        }
        val pushHistoryService = FakePushHistoryService(
            getPendingPushRequests = { _, _ -> Result.success(listOf(aPushRequest())) },
            replacePushRequests = { Result.success(Unit) },
            removeOldPushRequests = { Result.success(Unit) },
        )

        val worker = createWorker(
            input = "@alice:matrix.org",
            pushHistoryService = pushHistoryService,
            resultProcessor = processor,
            syncOnNotifiableEvent = syncOnNotifiableEventLambda,
            pushHandlingWakeLock = FakeFetchPushForegroundServiceManager(unlock = stopForegroundServiceLambda),
            buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(30),
        )

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())

        // The foreground service is released once, before the events are resolved, since its notification would be displayed immediately
        stopForegroundServiceLambda.assertions().isCalledOnce()
        assertThat(callOrder).containsExactly("stop", "emit", "sync").inOrder()
    }

    @Test
    fun `test - invalid input fails the work`() = runTest {
        val stopForegroundServiceLambda = lambdaRecorder<Boolean> { true }
        val worker = createWorker(
            input = "!alice:matrix.org",
            pushHandlingWakeLock = FakeFetchPushForegroundServiceManager(unlock = stopForegroundServiceLambda),
        )

        val result = worker.doWork()

        // The process failed
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())

        // The foreground service is still stopped
        stopForegroundServiceLambda.assertions().isCalledOnce()
    }

    @Test
    fun `test - no network connectivity fails the work`() = runTest {
        val networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Disconnected)
        val emitResultLambda = lambdaRecorder<Map<PushRequest, Result<ResolvedPushEvent>>, Unit> {}
        val processor = FakeNotificationResultProcessor(emit = emitResultLambda)
        val pushHistoryService = FakePushHistoryService(
            getPendingPushRequests = { _, _ -> Result.success(listOf(aPushRequest())) },
            replacePushRequests = { Result.success(Unit) },
            removeOldPushRequests = { Result.success(Unit) },
        )
        val stopForegroundServiceLambda = lambdaRecorder<Boolean> { true }
        val worker = createWorker(
            input = "@alice:matrix.org",
            networkMonitor = networkMonitor,
            resultProcessor = processor,
            pushHistoryService = pushHistoryService,
            pushHandlingWakeLock = FakeFetchPushForegroundServiceManager(unlock = stopForegroundServiceLambda),
        )

        val result = worker.doWork()

        advanceTimeBy(10.seconds)

        // The process failed due to a timeout in getting the network connectivity, a retry is scheduled
        assertThat(result).isEqualTo(ListenableWorker.Result.retry())

        // The foreground service is still stopped
        stopForegroundServiceLambda.assertions().isCalledOnce()
    }

    @Test
    fun `test - failing to setup retries the work`() = runTest {
        val submitWorkerLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
        val emitResultLambda = lambdaRecorder<Map<PushRequest, Result<ResolvedPushEvent>>, Unit> {}
        val processor = FakeNotificationResultProcessor(emit = emitResultLambda)
        val pushHistoryService = FakePushHistoryService(
            getPendingPushRequests = { _, _ -> Result.success(listOf(aPushRequest())) },
            replacePushRequests = { Result.success(Unit) },
            removeOldPushRequests = { Result.success(Unit) },
        )

        val resolver = FakeNotifiableEventResolver(
            resolveEventsResult = { _, _ -> Result.failure(Exception("Failed to resolve events")) }
        )

        val worker = createWorker(
            input = "@alice:matrix.org",
            eventResolver = resolver,
            resultProcessor = processor,
            pushHistoryService = pushHistoryService,
        )

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
        // Never called since we don't need to re-submit
        submitWorkerLambda.assertions().isNeverCalled()
    }

    @Test
    fun `test - failing to resolve events with recoverable error retries the work`() {
        val pushRequest = aPushRequest()
        runTest {
            val submitWorkerLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
            val emitResultLambda = lambdaRecorder<Map<PushRequest, Result<ResolvedPushEvent>>, Unit> {}
            val processor = FakeNotificationResultProcessor(emit = emitResultLambda)
            val pushHistoryService = FakePushHistoryService(
                getPendingPushRequests = { _, _ -> Result.success(listOf(pushRequest)) },
                replacePushRequests = { Result.success(Unit) },
                removeOldPushRequests = { Result.success(Unit) },
            )

            val resolver = FakeNotifiableEventResolver(
                resolveEventsResult = { _, _ ->
                    Result.success(mapOf(pushRequest to Result.failure(ClientException.Generic("error sending request for url", null))))
                }
            )

            val worker = createWorker(
                input = "@alice:matrix.org",
                eventResolver = resolver,
                resultProcessor = processor,
                pushHistoryService = pushHistoryService,
            )

            val result = worker.doWork()

            assertThat(result).isEqualTo(ListenableWorker.Result.retry())

            // Never called since we don't need to re-submit
            submitWorkerLambda.assertions().isNeverCalled()

            // We do save the updated events to the push DB
            emitResultLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `test - failing to resolve events with unrecoverable error saves the new state and ends as success`() {
        val pushRequest = aPushRequest()
        runTest {
            val submitWorkerLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
            val emitResultLambda = lambdaRecorder<Map<PushRequest, Result<ResolvedPushEvent>>, Unit> {}
            val processor = FakeNotificationResultProcessor(emit = emitResultLambda)
            val pushHistoryService = FakePushHistoryService(
                getPendingPushRequests = { _, _ -> Result.success(listOf(pushRequest)) },
                replacePushRequests = { Result.success(Unit) },
                removeOldPushRequests = { Result.success(Unit) },
            )

            val resolver = FakeNotifiableEventResolver(
                resolveEventsResult = { _, _ ->
                    Result.success(mapOf(pushRequest to Result.failure(IllegalStateException("Unrecoverable"))))
                }
            )

            val worker = createWorker(
                input = "@alice:matrix.org",
                eventResolver = resolver,
                resultProcessor = processor,
                pushHistoryService = pushHistoryService,
            )

            val result = worker.doWork()

            assertThat(result).isEqualTo(ListenableWorker.Result.success())

            // Never called since we don't need to re-submit
            submitWorkerLambda.assertions().isNeverCalled()

            // We do save the updated events to the push DB
            emitResultLambda.assertions().isCalledOnce()
        }
    }

    private fun TestScope.createWorker(
        input: String,
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(),
        eventResolver: FakeNotifiableEventResolver = FakeNotifiableEventResolver(resolveEventsResult = { _, _ -> Result.success(emptyMap()) }),
        syncOnNotifiableEvent: SyncOnNotifiableEvent = SyncOnNotifiableEvent {},
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
        pushHistoryService: FakePushHistoryService = FakePushHistoryService(),
        resultProcessor: FakeNotificationResultProcessor = FakeNotificationResultProcessor(),
        systemClock: FakeSystemClock = FakeSystemClock(),
        pushHandlingWakeLock: FakeFetchPushForegroundServiceManager = FakeFetchPushForegroundServiceManager(),
        buildVersionSdkIntProvider: FakeBuildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(33),
    ) = FetchPendingNotificationsWorker(
        params = createWorkerParams(workDataOf("session_id" to input)),
        context = InstrumentationRegistry.getInstrumentation().context,
        networkMonitor = networkMonitor,
        eventResolver = eventResolver,
        syncOnNotifiableEvent = syncOnNotifiableEvent,
        analyticsService = analyticsService,
        pushHistoryService = pushHistoryService,
        resultProcessor = resultProcessor,
        systemClock = systemClock,
        fetchPushForegroundServiceManager = pushHandlingWakeLock,
        buildVersionSdkIntProvider = buildVersionSdkIntProvider,
    )

    private fun TestScope.createWorkerParams(
        inputData: Data = Data.EMPTY,
    ): WorkerParameters = WorkerParameters(
        UUID.randomUUID(),
        inputData,
        emptySet(),
        WorkerParameters.RuntimeExtras(),
        0,
        0,
        Executors.newSingleThreadExecutor(),
        backgroundScope.coroutineContext,
        WorkManagerTaskExecutor(Executors.newSingleThreadExecutor()),
        MetroWorkerFactory(emptyMap()),
        { context, id, data -> FakeListenableFuture() },
        { context, id, foregroundInfo -> FakeListenableFuture() },
    )
}

class FakeListenableFuture<T> : ListenableFuture<T> {
    override fun addListener(listener: Runnable, executor: Executor) = Unit
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = true
    override fun get(): T? = null
    override fun get(timeout: Long, unit: TimeUnit?): T? = null
    override fun isCancelled(): Boolean = false
    override fun isDone(): Boolean = false
}
