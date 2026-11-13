/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.datasource

import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.libraries.mediaviewer.impl.model.MediaItem
import dev.zacsweers.metro.ContributesBinding

fun interface FocusedTimelineMediaGalleryDataSourceFactory {
    fun createFor(
        eventId: EventId,
        mediaItem: MediaItem.Event,
        onlyPinnedEvents: Boolean,
    ): MediaGalleryDataSource
}

@ContributesBinding(RoomScope::class)
class DefaultFocusedTimelineMediaGalleryDataSourceFactory(
    private val room: JoinedRoom,
    private val timelineMediaItemsFactory: TimelineMediaItemsFactory,
    private val mediaItemsPostProcessor: MediaItemsPostProcessor,
) : FocusedTimelineMediaGalleryDataSourceFactory {
    override fun createFor(
        eventId: EventId,
        mediaItem: MediaItem.Event,
        onlyPinnedEvents: Boolean,
    ): MediaGalleryDataSource {
        return TimelineMediaGalleryDataSource(
            room = room,
            mediaTimeline = FocusedMediaTimeline(
                room = room,
                eventId = eventId,
                initialMediaItem = mediaItem,
                onlyPinnedEvents = onlyPinnedEvents,
            ),
            timelineMediaItemsFactory = timelineMediaItemsFactory,
            mediaItemsPostProcessor = mediaItemsPostProcessor,
        )
    }
}
