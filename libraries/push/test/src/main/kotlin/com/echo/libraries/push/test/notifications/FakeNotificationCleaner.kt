/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.test.notifications

import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.push.api.notifications.NotificationCleaner
import com.echo.tests.testutils.lambda.lambdaError

class FakeNotificationCleaner(
    private val clearAllMessagesEventsLambda: (SessionId) -> Unit = { lambdaError() },
    private val clearMessagesForRoomLambda: (SessionId, RoomId) -> Unit = { _, _ -> lambdaError() },
    private val clearMessagesForThreadLambda: (SessionId, RoomId, ThreadId) -> Unit = { _, _, _ -> lambdaError() },
    private val clearEventLambda: (SessionId, EventId) -> Unit = { _, _ -> lambdaError() },
    private val clearMembershipNotificationForSessionLambda: (SessionId) -> Unit = { lambdaError() },
    private val clearMembershipNotificationForRoomLambda: (SessionId, RoomId) -> Unit = { _, _ -> lambdaError() },
    private val clearReadRoomsNotificationsLambda: (SessionId) -> Unit = { lambdaError() },
) : NotificationCleaner {
    override fun clearAllMessagesEvents(sessionId: SessionId) {
        clearAllMessagesEventsLambda(sessionId)
    }

    override fun clearMessagesForRoom(sessionId: SessionId, roomId: RoomId) {
        clearMessagesForRoomLambda(sessionId, roomId)
    }

    override fun clearMessagesForThread(sessionId: SessionId, roomId: RoomId, threadId: ThreadId) {
        clearMessagesForThreadLambda(sessionId, roomId, threadId)
    }

    override fun clearEvent(sessionId: SessionId, eventId: EventId) {
        clearEventLambda(sessionId, eventId)
    }

    override fun clearMembershipNotificationForSession(sessionId: SessionId) {
        clearMembershipNotificationForSessionLambda(sessionId)
    }

    override fun clearMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId) {
        clearMembershipNotificationForRoomLambda(sessionId, roomId)
    }

    override fun clearReadRoomsNotifications(sessionId: SessionId) {
        clearReadRoomsNotificationsLambda(sessionId)
    }
}

