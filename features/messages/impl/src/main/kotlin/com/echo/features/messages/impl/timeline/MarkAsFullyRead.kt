/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline

import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext
import timber.log.Timber

interface MarkAsFullyRead {
    suspend operator fun invoke(roomId: RoomId, eventId: EventId): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultMarkAsFullyRead(
    private val matrixClient: MatrixClient,
    private val coroutineDispatchers: CoroutineDispatchers,
) : MarkAsFullyRead {
    override suspend fun invoke(roomId: RoomId, eventId: EventId): Result<Unit> = withContext(coroutineDispatchers.io) {
        matrixClient.markRoomAsFullyRead(roomId, eventId).onFailure {
            Timber.e(it, "Failed to mark room $roomId as fully read for event $eventId")
        }
    }
}
