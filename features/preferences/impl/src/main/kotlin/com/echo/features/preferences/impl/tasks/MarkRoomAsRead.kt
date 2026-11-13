/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.tasks

import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.timeline.ReceiptType
import com.echo.libraries.preferences.api.store.SessionPreferencesStore
import com.echo.libraries.push.api.notifications.NotificationCleaner
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

interface MarkRoomAsRead {
    suspend operator fun invoke(roomId: RoomId): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultMarkRoomAsRead(
    private val client: MatrixClient,
    private val notificationCleaner: NotificationCleaner,
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val coroutineDispatchers: CoroutineDispatchers,
) : MarkRoomAsRead {
    override suspend fun invoke(roomId: RoomId): Result<Unit> = withContext(coroutineDispatchers.io) {
        notificationCleaner.clearMessagesForRoom(client.sessionId, roomId)
        val room = client.getRoom(roomId) ?: return@withContext Result.failure(IllegalStateException("Room not found"))
        room.use {
            it.setUnreadFlag(isUnread = false)
            val receiptType = if (sessionPreferencesStore.isSendPublicReadReceiptsEnabled().first()) {
                ReceiptType.READ
            } else {
                ReceiptType.READ_PRIVATE
            }
            it.markAsRead(receiptType)
        }
    }
}
