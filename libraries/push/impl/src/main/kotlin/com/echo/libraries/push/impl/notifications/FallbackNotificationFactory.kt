/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.notifications

import dev.zacsweers.metro.Inject
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import com.echo.services.toolbox.api.systemclock.SystemClock

@Inject
class FallbackNotificationFactory(
    private val clock: SystemClock,
) {
    fun create(
        sessionId: SessionId,
        roomId: RoomId,
        eventId: EventId,
        cause: String?,
        noisy: Boolean,
    ): FallbackNotifiableEvent = FallbackNotifiableEvent(
        sessionId = sessionId,
        roomId = roomId,
        eventId = eventId,
        editedEventId = null,
        canBeReplaced = true,
        isRedacted = false,
        isUpdated = false,
        noisy = noisy,
        timestamp = clock.epochMillis(),
        description = "",
        cause = cause,
    )
}

