/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.echo.features.messages.impl.timeline.aTimelineItemEvent
import com.echo.features.messages.impl.timeline.model.TimelineItemGroupPosition
import com.echo.features.messages.impl.timeline.model.event.TimelineItemEventContent
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithGalleryPreview(
    @PreviewParameter(TimelineItemEventContentForGalleryViewPreviewParam::class) content: TimelineItemEventContent,
) = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = content,
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

