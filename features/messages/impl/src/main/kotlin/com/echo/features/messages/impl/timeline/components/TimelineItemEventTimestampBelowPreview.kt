/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.echo.features.messages.impl.timeline.aTimelineItemEvent
import com.echo.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import com.echo.libraries.designsystem.preview.ElementPreviewLight

// Note: no need for light/dark variant for this preview, we only look at the timestamp position
@Preview
@Composable
internal fun TimelineItemEventTimestampBelowPreview() = ElementPreviewLight {
    ATimelineItemEventRow(
        event = aTimelineItemEvent(content = aTimelineItemPollContent()),
    )
}

