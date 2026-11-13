/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.share.impl

import com.echo.features.share.api.OnSharedData
import com.echo.features.share.api.ShareIntentData
import com.echo.libraries.core.bool.orFalse
import com.echo.libraries.core.extensions.runCatchingExceptions
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.libraries.mediaupload.api.MediaOptimizationConfigProviderFactory
import com.echo.libraries.mediaupload.api.MediaSenderRoomFactory
import com.echo.services.appnavstate.api.ActiveRoomsHolder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlin.coroutines.cancellation.CancellationException

@ContributesBinding(AppScope::class)
class DefaultShareDataSender(
    private val mediaSenderRoomFactory: MediaSenderRoomFactory,
    private val mediaOptimizationConfigProviderFactory: MediaOptimizationConfigProviderFactory,
    private val activeRoomsHolder: ActiveRoomsHolder,
    private val onSharedData: OnSharedData,
) : ShareDataSender {
    override suspend fun send(
        client: MatrixClient,
        shareIntentData: ShareIntentData,
        roomIds: List<RoomId>,
    ): Result<Unit> = runCatchingExceptions {
        val result = when (shareIntentData) {
            is ShareIntentData.PlainText -> {
                roomIds
                    .map { roomId ->
                        client.findJoinedRoom(roomId)?.liveTimeline?.sendMessage(
                            body = shareIntentData.content,
                            htmlBody = null,
                            intentionalMentions = emptyList(),
                        )?.isSuccess.orFalse()
                    }
                    .all { it }
            }
            is ShareIntentData.Uris -> {
                val filesToShare = shareIntentData.uris
                if (filesToShare.isEmpty()) {
                    false
                } else {
                    // The media optimization config is the same for all the files and all the rooms
                    val mediaOptimizationConfig = mediaOptimizationConfigProviderFactory.create(client).get()
                    roomIds
                        .map { roomId ->
                            val room = client.findJoinedRoom(roomId) ?: return@map false
                            val mediaSender = mediaSenderRoomFactory.create(client = client, room = room)
                            filesToShare
                                .map { fileToShare ->
                                    val result = mediaSender.sendMedia(
                                        caption = shareIntentData.text,
                                        uri = fileToShare.uri,
                                        mimeType = fileToShare.mimeType,
                                        mediaOptimizationConfig = mediaOptimizationConfig,
                                    )
                                    // If the coroutine was cancelled, destroy the room and rethrow the exception
                                    val cancellationException = result.exceptionOrNull() as? CancellationException
                                    if (cancellationException != null) {
                                        if (activeRoomsHolder.getActiveRoomMatching(client.sessionId, roomId) == null) {
                                            room.destroy()
                                        }
                                        throw cancellationException
                                    }
                                    result.isSuccess
                                }
                                .all { isSuccess -> isSuccess }
                                .also {
                                    if (activeRoomsHolder.getActiveRoomMatching(client.sessionId, roomId) == null) {
                                        room.destroy()
                                    }
                                }
                        }
                        .all { it }
                }
            }
        }

        // Handle post-processing of shared data
        onSharedData(shareIntentData)

        if (!result) {
            error("Failed to handle incoming share intent")
        }
    }

    private suspend fun MatrixClient.findJoinedRoom(roomId: RoomId): JoinedRoom? {
        return activeRoomsHolder.getActiveRoom(sessionId)
            ?.takeIf { it.roomId == roomId }
            ?: getJoinedRoom(roomId)
    }
}
