/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.user

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.matrix.ui.components.MatrixUserHeader
import com.echo.libraries.matrix.ui.components.MatrixUserPreviewParam

@Composable
fun UserPreferences(
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
) {
    MatrixUserHeader(
        modifier = modifier,
        matrixUser = matrixUser,
    )
}

@PreviewsDayNight
@Composable
internal fun UserPreferencesPreview(@PreviewParameter(MatrixUserPreviewParam::class) matrixUser: MatrixUser) = ElementPreview {
    UserPreferences(matrixUser)
}

