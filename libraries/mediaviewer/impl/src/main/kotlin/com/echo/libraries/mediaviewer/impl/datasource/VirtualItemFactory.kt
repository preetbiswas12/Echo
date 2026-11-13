/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.datasource

import com.echo.libraries.dateformatter.api.DateFormatter
import com.echo.libraries.dateformatter.api.DateFormatterMode
import com.echo.libraries.matrix.api.timeline.MatrixTimelineItem
import com.echo.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import com.echo.libraries.mediaviewer.impl.model.MediaItem
import dev.zacsweers.metro.Inject

@Inject
class VirtualItemFactory(
    private val dateFormatter: DateFormatter,
) {
    fun create(timelineItem: MatrixTimelineItem.Virtual): MediaItem? {
        return when (val virtual = timelineItem.virtual) {
            is VirtualTimelineItem.DayDivider -> MediaItem.DateSeparator(
                id = timelineItem.uniqueId,
                formattedDate = dateFormatter.format(
                    timestamp = virtual.timestamp,
                    mode = DateFormatterMode.Month,
                    useRelative = true,
                )
            )
            VirtualTimelineItem.LastForwardIndicator -> null
            is VirtualTimelineItem.LoadingIndicator -> MediaItem.LoadingIndicator(
                id = timelineItem.uniqueId,
                direction = virtual.direction,
                timestamp = virtual.timestamp
            )
            VirtualTimelineItem.ReadMarker -> null
            VirtualTimelineItem.RoomBeginning -> null
            VirtualTimelineItem.TypingNotification -> null
        }
    }
}
