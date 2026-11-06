/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.designsystem.theme.components.previews

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.echo.compound.theme.ElementTheme
import com.echo.libraries.architecture.coverage.ExcludeFromCoverage
import com.echo.libraries.designsystem.preview.ElementPreviewDark
import com.echo.libraries.designsystem.preview.ElementPreviewLight
import com.echo.libraries.designsystem.preview.PreviewGroup
import com.echo.libraries.designsystem.theme.components.AlertDialogContent

@Preview(group = PreviewGroup.DateTimePickers)
@Composable
internal fun DatePickerLightPreview() {
    ElementPreviewLight { ContentToPreview() }
}

@Preview(group = PreviewGroup.DateTimePickers)
@Composable
internal fun DatePickerDarkPreview() {
    ElementPreviewDark { ContentToPreview() }
}

@ExcludeFromCoverage
@Composable
private fun ContentToPreview() {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = 1_672_578_000_000L,
    )
    AlertDialogContent(
        buttons = { /*TODO*/ },
        icon = { /*TODO*/ },
        title = { /*TODO*/ },
        subtitle = null,
        content = { DatePicker(state = state, showModeToggle = true) },
        shape = AlertDialogDefaults.shape,
        containerColor = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
        buttonContentColor = ElementTheme.colors.textPrimary,
        iconContentColor = AlertDialogDefaults.iconContentColor,
        titleContentColor = AlertDialogDefaults.titleContentColor,
        textContentColor = AlertDialogDefaults.textContentColor,
    )
}

