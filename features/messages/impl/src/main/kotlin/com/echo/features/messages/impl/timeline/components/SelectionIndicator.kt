/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.Checkbox

/**
 * Selection indicator used in the timeline selection mode. It renders the Compound [Checkbox], with the minimum
 * interactive size enforcement disabled so that it fits the compact selection column. It is purely visual: the tap
 * is handled by the whole-row click target, so this only reflects [selected].
 */
@Composable
internal fun SelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Checkbox(
            modifier = modifier,
            checked = selected,
            onCheckedChange = null,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SelectionIndicatorPreview() = ElementPreview {
    SelectionIndicator(selected = false)
}

@PreviewsDayNight
@Composable
internal fun SelectionIndicatorSelectedPreview() = ElementPreview {
    SelectionIndicator(selected = true)
}

