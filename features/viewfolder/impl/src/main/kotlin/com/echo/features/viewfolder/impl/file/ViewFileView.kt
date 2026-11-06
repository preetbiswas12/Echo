/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.viewfolder.impl.file

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.designsystem.components.async.AsyncFailure
import com.echo.libraries.designsystem.components.async.AsyncLoading
import com.echo.libraries.designsystem.components.button.BackButton
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.Icon
import com.echo.libraries.designsystem.theme.components.IconButton
import com.echo.libraries.designsystem.theme.components.Scaffold
import com.echo.libraries.designsystem.theme.components.TopAppBar
import com.echo.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ViewFileView(
    state: ViewFileState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClick)
                },
                titleStr = state.name,
                actions = {
                    IconButton(
                        onClick = {
                            state.eventSink(ViewFileEvent.Share)
                        },
                    ) {
                        Icon(
                            imageVector = CompoundIcons.ShareAndroid(),
                            contentDescription = stringResource(id = CommonStrings.action_share),
                        )
                    }
                    IconButton(
                        onClick = {
                            state.eventSink(ViewFileEvent.SaveOnDisk)
                        },
                    ) {
                        Icon(
                            imageVector = CompoundIcons.Download(),
                            contentDescription = stringResource(id = CommonStrings.action_save),
                        )
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            ) {
                when (state.lines) {
                    AsyncData.Uninitialized,
                    is AsyncData.Loading -> AsyncLoading()
                    is AsyncData.Success -> FileContent(
                        modifier = Modifier.weight(1f),
                        lines = state.lines.data.toImmutableList(),
                        colorationMode = state.colorationMode,
                        onLineClick = { state.eventSink(ViewFileEvent.CopyToClipboard(it)) },
                    )
                    is AsyncData.Failure -> AsyncFailure(throwable = state.lines.error, onRetry = null)
                }
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun ViewFileViewPreview(@PreviewParameter(ViewFileStatePreviewParam::class) state: ViewFileState) = ElementPreview {
    ViewFileView(
        state = state,
        onBackClick = {},
    )
}

