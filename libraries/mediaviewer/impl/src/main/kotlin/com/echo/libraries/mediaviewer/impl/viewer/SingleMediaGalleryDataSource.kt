/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.viewer

import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.core.mimetype.MimeTypes
import com.echo.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import com.echo.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import com.echo.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.UniqueId
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import com.echo.libraries.matrix.ui.media.contentvalidation.DefaultContentValidationState
import com.echo.libraries.matrix.ui.media.contentvalidation.EventContentValidationCache
import com.echo.libraries.mediaviewer.api.MediaInfo
import com.echo.libraries.mediaviewer.api.MediaViewerEntryPoint
import com.echo.libraries.mediaviewer.impl.datasource.MediaGalleryDataSource
import com.echo.libraries.mediaviewer.impl.model.GroupedMediaItems
import com.echo.libraries.mediaviewer.impl.model.MediaItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf

class SingleMediaGalleryDataSource(
    private val data: GroupedMediaItems,
    private val contentValidationCache: EventContentValidationCache,
) : MediaGalleryDataSource {
    override fun start(coroutineScope: CoroutineScope) = Unit
    override fun groupedMediaItemsFlow() = flowOf(AsyncData.Success(data))
    override fun getLastData(): AsyncData<GroupedMediaItems> = AsyncData.Success(data)

    override val isReady: Boolean = true

    override suspend fun loadMore(direction: Timeline.PaginationDirection) = Unit
    override suspend fun deleteItem(eventId: EventId) = Unit

    companion object {
        fun createFrom(
            params: MediaViewerEntryPoint.Params.Avatar,
            contentValidationCache: EventContentValidationCache,
        ) = SingleMediaGalleryDataSource(
            data = GroupedMediaItems(
                // Always use imageAndVideoItems, in Single mode, this is the data that will be used
                imageAndVideoItems = persistentListOf(
                    MediaItem.Image(
                        id = UniqueId("dummy"),
                        eventId = null,
                        mediaInfo = MediaInfo(
                            filename = params.avatarInfo.filename,
                            caption = null,
                            htmlCaption = null,
                            mimeType = MimeTypes.Images,
                            fileSize = null,
                            formattedFileSize = "",
                            fileExtension = "",
                            senderId = null,
                            senderName = null,
                            senderAvatar = null,
                            dateSent = null,
                            dateSentFull = null,
                            waveform = null,
                            duration = null,
                        ),
                        mediaSource = params.mediaSource,
                        thumbnailSource = params.thumbnailSource,
                        blurHash = params.blurHash,
                        validationState = DefaultContentValidationState(),
                    )
                ),
                fileItems = persistentListOf(),
            ),
            contentValidationCache = contentValidationCache,
        )
    }
}

fun MediaViewerEntryPoint.Params.RoomMedia.toMediaItem(
    contentValidationState: ContentValidationState,
) = when {
    mediaInfo.mimeType.isMimeTypeImage() -> {
        MediaItem.Image(
            id = UniqueId("dummy"),
            eventId = eventId,
            mediaInfo = mediaInfo,
            mediaSource = mediaSource,
            thumbnailSource = thumbnailSource,
            blurHash = null,
            validationState = contentValidationState,
        )
    }
    mediaInfo.mimeType.isMimeTypeVideo() -> {
        MediaItem.Video(
            id = UniqueId("dummy"),
            eventId = eventId,
            mediaInfo = mediaInfo,
            mediaSource = mediaSource,
            thumbnailSource = thumbnailSource,
            blurHash = null,
            validationState = contentValidationState,
        )
    }
    mediaInfo.mimeType.isMimeTypeAudio() -> {
        if (mediaInfo.waveform == null) {
            MediaItem.Audio(
                id = UniqueId("dummy"),
                eventId = eventId,
                mediaInfo = mediaInfo,
                mediaSource = mediaSource,
                validationState = contentValidationState,
            )
        } else {
            MediaItem.Voice(
                id = UniqueId("dummy"),
                eventId = eventId,
                mediaInfo = mediaInfo,
                mediaSource = mediaSource,
                validationState = contentValidationState,
            )
        }
    }
    else -> {
        MediaItem.File(
            id = UniqueId("dummy"),
            eventId = eventId,
            mediaInfo = mediaInfo,
            mediaSource = mediaSource,
            validationState = contentValidationState,
        )
    }
}

