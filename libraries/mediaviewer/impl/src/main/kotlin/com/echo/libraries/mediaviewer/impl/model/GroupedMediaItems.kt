/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.model

import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.mediaviewer.impl.gallery.MediaGalleryMode
import kotlinx.collections.immutable.ImmutableList

data class GroupedMediaItems(
    val imageAndVideoItems: ImmutableList<MediaItem>,
    val fileItems: ImmutableList<MediaItem>,
) {
    fun getItems(mode: MediaGalleryMode): ImmutableList<MediaItem> {
        return when (mode) {
            MediaGalleryMode.Images -> imageAndVideoItems
            MediaGalleryMode.Files -> fileItems
        }
    }
}

fun GroupedMediaItems.hasEvent(eventId: EventId): Boolean {
    return (fileItems + imageAndVideoItems)
        .filterIsInstance<MediaItem.Event>()
        .any { it.eventId() == eventId }
}
