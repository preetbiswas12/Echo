/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.designsystem.theme.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.echo.compound.theme.ElementTheme

@Composable
fun NavigationBarText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text,
        style = ElementTheme.typography.fontBodySmMedium,
    )
}

