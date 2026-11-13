/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.timeline.postprocessor

import com.echo.libraries.matrix.api.core.UniqueId
import com.echo.libraries.matrix.api.timeline.MatrixTimelineItem
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import com.echo.services.toolbox.api.systemclock.SystemClock

class LoadingIndicatorsPostProcessor(private val systemClock: SystemClock) {
    fun process(
        items: List<MatrixTimelineItem>,
        hasMoreToLoadBackward: Boolean,
        hasMoreToLoadForward: Boolean,
    ): List<MatrixTimelineItem> {
        val shouldAddForwardLoadingIndicator = hasMoreToLoadForward && items.isNotEmpty()
        val currentTimestamp = systemClock.epochMillis()
        return buildList {
            if (hasMoreToLoadBackward) {
                val backwardLoadingIndicator = MatrixTimelineItem.Virtual(
                    uniqueId = UniqueId("BackwardLoadingIndicator"),
                    virtual = VirtualTimelineItem.LoadingIndicator(
                        direction = Timeline.PaginationDirection.BACKWARDS,
                        timestamp = currentTimestamp
                    )
                )
                add(backwardLoadingIndicator)
            }
            addAll(items)
            if (shouldAddForwardLoadingIndicator) {
                val forwardLoadingIndicator = MatrixTimelineItem.Virtual(
                    uniqueId = UniqueId("ForwardLoadingIndicator"),
                    virtual = VirtualTimelineItem.LoadingIndicator(
                        direction = Timeline.PaginationDirection.FORWARDS,
                        timestamp = currentTimestamp
                    )
                )
                add(forwardLoadingIndicator)
            }
        }
    }
}
