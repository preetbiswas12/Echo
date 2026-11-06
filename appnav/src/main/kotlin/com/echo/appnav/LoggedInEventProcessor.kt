/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.appnav

import androidx.annotation.VisibleForTesting
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.core.coroutine.childScope
import com.echo.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import com.echo.libraries.designsystem.utils.snackbar.SnackbarMessage
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.room.RoomMembershipObserver
import com.echo.libraries.matrix.api.timeline.item.event.MembershipChange
import com.echo.libraries.push.api.notifications.NotificationCleaner
import com.echo.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AssistedInject
class LoggedInEventProcessor(
    @Assisted private val snackbarDispatcher: SnackbarDispatcher,
    private val roomMembershipObserver: RoomMembershipObserver,
    private val matrixClient: MatrixClient,
    private val notificationCleaner: NotificationCleaner,
    private val dispatchers: CoroutineDispatchers
) {
    @AssistedFactory
    interface Factory {
        fun create(snackbarDispatcher: SnackbarDispatcher): LoggedInEventProcessor
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var currentChildScope: CoroutineScope? = null

    fun observeEvents(coroutineScope: CoroutineScope) {
        if (currentChildScope != null) return

        val childScope = coroutineScope.childScope(dispatchers.computation, "LoggedInEventProcessor")
            .also { currentChildScope = it }

        roomMembershipObserver.updates
            .filter { !it.isUserInRoom }
            .distinctUntilChanged()
            .onEach { roomMemberShipUpdate ->
                when (roomMemberShipUpdate.change) {
                    MembershipChange.LEFT -> {
                        displayMessage(
                            if (roomMemberShipUpdate.isSpace) {
                                CommonStrings.common_current_user_left_space
                            } else {
                                CommonStrings.common_current_user_left_room
                            }
                        )
                    }
                    MembershipChange.INVITATION_REJECTED -> displayMessage(CommonStrings.common_current_user_rejected_invite)
                    MembershipChange.KNOCK_RETRACTED -> displayMessage(CommonStrings.common_current_user_canceled_knock)
                    else -> Unit
                }
            }
            .launchIn(childScope)

        // Use the sync service heartbeat as a 'heartbeat' update to check existing notifications
        matrixClient.totalUnreadCountFlow
            // We drop the first value because it is the initial one and we don't want to clear notifications on startup
            .drop(1)
            .onEach {
                notificationCleaner.clearReadRoomsNotifications(matrixClient.sessionId)
            }
            .launchIn(childScope)
    }

    fun stopObserving() {
        currentChildScope?.cancel()
        currentChildScope = null
    }

    private fun displayMessage(message: Int) {
        snackbarDispatcher.post(SnackbarMessage(message))
    }
}

