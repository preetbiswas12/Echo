/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.classic.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.CircularProgressIndicator
import com.echo.libraries.designsystem.utils.DelayedVisibility
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RootView(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        DelayedVisibility(
            duration = 100.milliseconds,
        ) {
            CircularProgressIndicator()
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RootViewPreview() = ElementPreview {
    RootView()
}

