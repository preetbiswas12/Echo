/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.test.notifications

import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.push.api.notifications.OnMissedCallNotificationHandler

class FakeOnMissedCallNotificationHandler(
    var addMissedCallNotificationLambda: (SessionId, RoomId, EventId) -> Unit = { _, _, _ -> }
) : OnMissedCallNotificationHandler {
    override suspend fun addMissedCallNotification(
        sessionId: SessionId,
        roomId: RoomId,
        eventId: EventId,
    ) {
        addMissedCallNotificationLambda(sessionId, roomId, eventId)
    }
}

