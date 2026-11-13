/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.designsystem.utils.snackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.libraries.designsystem.components.button.ButtonVisuals
import com.echo.libraries.designsystem.theme.components.IconSource
import com.echo.libraries.designsystem.theme.components.Snackbar

@Composable
fun SnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    androidx.compose.material3.SnackbarHost(hostState, modifier) { data ->
        Snackbar(
            // Add default padding
            modifier = Modifier.padding(12.dp),
            message = data.visuals.message,
            action = data.visuals.actionLabel?.let { ButtonVisuals.Text(it, data::performAction) },
            dismissAction = if (data.visuals.withDismissAction) {
                ButtonVisuals.Icon(
                    IconSource.Vector(CompoundIcons.Close()),
                    data::dismiss
                )
            } else {
                null
            },
        )
    }
}
