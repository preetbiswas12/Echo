/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.forward.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.echo.libraries.designsystem.components.async.AsyncActionView
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.ui.strings.CommonStrings

@Composable
fun ForwardMessagesView(
    state: ForwardMessagesState,
    onForwardSuccess: (List<RoomId>) -> Unit,
) {
    AsyncActionView(
        async = state.forwardAction,
        onSuccess = {
            onForwardSuccess(it)
        },
        errorMessage = {
            stringResource(id = CommonStrings.error_unknown)
        },
        onErrorDismiss = {
            state.eventSink(ForwardMessagesEvent.ClearError)
        },
    )
}

@PreviewsDayNight
@Composable
internal fun ForwardMessagesViewPreview(@PreviewParameter(ForwardMessagesStatePreviewParam::class) state: ForwardMessagesState) = ElementPreview {
    ForwardMessagesView(
        state = state,
        onForwardSuccess = {}
    )
}

