/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.push

import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.matrix.api.MatrixClientProvider
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.push.impl.db.PushRequest
import com.echo.libraries.push.impl.history.PushHistoryService
import com.echo.services.appnavstate.api.AppForegroundStateService
import com.echo.services.toolbox.api.systemclock.SystemClock
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private const val MAX_ROOMS_TO_SUBSCRIBE_TO = 20
private const val SYNC_TIMEOUT_SECONDS = 10

@ContributesBinding(AppScope::class)
class DefaultSyncOnNotifiableEvent(
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
    private val appForegroundStateService: AppForegroundStateService,
    private val dispatchers: CoroutineDispatchers,
    private val pushHistoryService: PushHistoryService,
    private val systemClock: SystemClock,
) : SyncOnNotifiableEvent {
    override suspend operator fun invoke(requests: List<PushRequest>) = withContext(dispatchers.io) {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncOnPush)) {
            return@withContext
        }

        val start = Instant.fromEpochMilliseconds(systemClock.epochMillis())
        val syncJobs = mutableListOf<Job>()
        try {
            val requestsBySessionId = requests.groupBy { it.sessionId }

            appForegroundStateService.updateIsSyncingNotificationEvent(true)

            if (!appForegroundStateService.isInForeground.value) {
                for ((sessionId, requests) in requestsBySessionId) {
                    Timber.d("Starting opportunistic room list sync for $sessionId")
                    syncJobs += launch {
                        syncUsingSession(SessionId(sessionId), requests)
                    }
                }

                syncJobs.joinAll()
            }
        } finally {
            if (syncJobs.isNotEmpty()) {
                val end = Instant.fromEpochMilliseconds(systemClock.epochMillis())
                Timber.d("Finished opportunistic room list syncs in ${end - start}")
            }
            appForegroundStateService.updateIsSyncingNotificationEvent(false)
        }
    }

    private suspend fun syncUsingSession(sessionId: SessionId, requests: List<PushRequest>) = withTimeoutOrNull(SYNC_TIMEOUT_SECONDS.seconds) {
        if (requests.isEmpty()) return@withTimeoutOrNull

        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return@withTimeoutOrNull

        val mostRecentRoomIds = client.roomListService.allRooms.summaries.first { it.isNotEmpty() }
            .take(MAX_ROOMS_TO_SUBSCRIBE_TO)
            .map { it.roomId }
        client.roomListService.subscribeToVisibleRooms(mostRecentRoomIds)

        // Give the sync some time to complete in background, but stop early if a newer push is waiting: the work fetching it can only
        // start once this one is done, and an incoming call can't wait that long to ring.
        // Pushes handled by this work and pending again after a network error keep their push date, so they don't end it early.
        val newestHandledPushDate = Instant.fromEpochMilliseconds(requests.maxOf { it.pushDate })

        val newPushQueued = pushHistoryService.getPendingPushRequestsFlow(sessionId, since = newestHandledPushDate)
            .first { it.isNotEmpty() }
            .isNotEmpty()
        if (newPushQueued) {
            Timber.d("A newer push is waiting to be fetched, ending the opportunistic sync early")
            return@withTimeoutOrNull
        }
    }
}
