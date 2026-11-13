/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.compound.theme.ElementTheme
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.mediaviewer.impl.model.MediaItem

@Composable
fun DateItemView(
    item: MediaItem.DateSeparator,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .semantics {
                heading()
            },
        text = item.formattedDate,
        textAlign = TextAlign.Center,
        style = ElementTheme.typography.fontBodyMdMedium,
        color = ElementTheme.colors.textPrimary,
    )
}

@PreviewsDayNight
@Composable
internal fun DateItemViewPreview(
    @PreviewParameter(MediaItemDateSeparatorPreviewParam::class) date: MediaItem.DateSeparator,
) = ElementPreview {
    DateItemView(date)
}
