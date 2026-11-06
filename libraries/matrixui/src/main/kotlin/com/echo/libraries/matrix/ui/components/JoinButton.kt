/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.echo.compound.theme.ElementTheme
import com.echo.libraries.designsystem.theme.components.ButtonSize
import com.echo.libraries.designsystem.theme.components.TextButton
import com.echo.libraries.ui.strings.CommonStrings

@Composable
fun JoinButton(
    showProgress: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.textActionAccent) {
        TextButton(
            modifier = modifier,
            text = stringResource(CommonStrings.action_join),
            onClick = onClick,
            size = ButtonSize.LargeLowPadding,
            showProgress = showProgress,
        )
    }
}

