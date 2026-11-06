/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.factories.virtual

import dev.zacsweers.metro.Inject
import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.features.messages.impl.timeline.model.virtual.TimelineItemLastForwardIndicatorModel
import com.echo.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import com.echo.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import com.echo.features.messages.impl.timeline.model.virtual.TimelineItemRoomBeginningModel
import com.echo.features.messages.impl.timeline.model.virtual.TimelineItemTypingNotificationModel
import com.echo.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import com.echo.libraries.matrix.api.timeline.MatrixTimelineItem
import com.echo.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

@Inject
class TimelineItemVirtualFactory(
    private val daySeparatorFactory: TimelineItemDaySeparatorFactory,
) {
    fun create(
        virtualTimelineItem: MatrixTimelineItem.Virtual,
    ): TimelineItem.Virtual {
        return TimelineItem.Virtual(
            id = virtualTimelineItem.uniqueId,
            model = virtualTimelineItem.computeModel()
        )
    }

    private fun MatrixTimelineItem.Virtual.computeModel(): TimelineItemVirtualModel {
        return when (val inner = virtual) {
            is VirtualTimelineItem.DayDivider -> daySeparatorFactory.create(inner)
            is VirtualTimelineItem.ReadMarker -> TimelineItemReadMarkerModel
            is VirtualTimelineItem.RoomBeginning -> TimelineItemRoomBeginningModel
            is VirtualTimelineItem.LoadingIndicator -> TimelineItemLoadingIndicatorModel(
                direction = inner.direction,
                timestamp = inner.timestamp
            )
            is VirtualTimelineItem.LastForwardIndicator -> TimelineItemLastForwardIndicatorModel
            VirtualTimelineItem.TypingNotification -> TimelineItemTypingNotificationModel
        }
    }
}

