/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.designsystem.components.async

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.libraries.architecture.AsyncAction

open class AsyncActionPreviewParam : PreviewParameterProvider<AsyncAction<Unit>> {
    override val values: Sequence<AsyncAction<Unit>>
        get() = sequenceOf(
            AsyncAction.Uninitialized,
            AsyncAction.ConfirmingNoParams,
            AsyncAction.Loading,
            AsyncAction.Failure(Exception("An error occurred")),
            AsyncAction.Success(Unit),
        )
}

