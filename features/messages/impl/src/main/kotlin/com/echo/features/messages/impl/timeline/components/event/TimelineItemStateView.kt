/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.echo.compound.theme.ElementTheme
import com.echo.features.messages.impl.timeline.model.event.TimelineItemStateContent
import com.echo.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.Text

@Composable
fun TimelineItemStateView(
    content: TimelineItemStateContent,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        color = ElementTheme.colors.textSecondary,
        style = ElementTheme.typography.fontBodyMdRegular,
        text = content.body,
        textAlign = TextAlign.Center,
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemStateViewPreview() = ElementPreview {
    TimelineItemStateView(
        content = aTimelineItemStateEventContent(),
    )
}

