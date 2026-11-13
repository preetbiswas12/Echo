/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.home.impl.components.RoomSummaryRow
import com.echo.features.home.impl.contentType
import com.echo.features.home.impl.model.RoomListRoomSummary
import com.echo.features.home.impl.roomlist.RoomListEvent
import com.echo.libraries.designsystem.components.button.BackButton
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.FilledTextField
import com.echo.libraries.designsystem.theme.components.Icon
import com.echo.libraries.designsystem.theme.components.IconButton
import com.echo.libraries.designsystem.theme.components.Scaffold
import com.echo.libraries.designsystem.theme.components.TopAppBar
import com.echo.libraries.designsystem.utils.OnVisibleRangeChangeEffect
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.ui.strings.CommonStrings

@Composable
internal fun RoomListSearchView(
    state: RoomListSearchState,
    hideInvitesAvatars: Boolean,
    eventSink: (RoomListEvent) -> Unit,
    onRoomClick: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.isSearchActive) {
        state.eventSink(RoomListSearchEvent.ToggleSearchVisibility)
    }

    AnimatedVisibility(
        visible = state.isSearchActive,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(modifier = modifier) {
            RoomListSearchContent(
                state = state,
                hideInvitesAvatars = hideInvitesAvatars,
                onRoomClick = onRoomClick,
                eventSink = eventSink,
            )
        }
    }
}

@Composable
private fun RoomListSearchContent(
    state: RoomListSearchState,
    hideInvitesAvatars: Boolean,
    eventSink: (RoomListEvent) -> Unit,
    onRoomClick: (RoomId) -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.tertiary
    val strokeWidth = 1.dp
    fun onBackButtonClick() {
        state.eventSink(RoomListSearchEvent.ToggleSearchVisibility)
    }

    fun onRoomClick(room: RoomListRoomSummary) {
        onRoomClick(room.roomId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth.value
                    )
                },
                navigationIcon = { BackButton(onClick = ::onBackButtonClick) },
                title = {
                    // The stateSaver will keep the selection state when returning to this UI
                    val focusRequester = remember { FocusRequester() }
                    val searchLabel = stringResource(CommonStrings.action_search)
                    FilledTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .semantics { contentDescription = searchLabel },
                        state = state.query,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                        ),
                        trailingIcon = if (state.query.text.isNotEmpty()) {
                            @Composable {
                                IconButton(onClick = { state.eventSink(RoomListSearchEvent.ClearQuery) }) {
                                    Icon(
                                        imageVector = CompoundIcons.Close(),
                                        contentDescription = stringResource(CommonStrings.a11y_clear_search_field)
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    )

                    LaunchedEffect(Unit) {
                        if (!focusRequester.restoreFocusedChild()) {
                            focusRequester.requestFocus()
                        }
                        focusRequester.saveFocusedChild()
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            val lazyListState = rememberLazyListState()
            OnVisibleRangeChangeEffect(lazyListState) { visibleRange ->
                state.eventSink(RoomListSearchEvent.UpdateVisibleRange(visibleRange))
            }
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            ) {
                items(
                    items = state.results,
                    contentType = { room -> room.contentType() },
                ) { room ->
                    RoomSummaryRow(
                        room = room,
                        hideInviteAvatars = hideInvitesAvatars,
                        // TODO
                        isInviteSeen = false,
                        onClick = ::onRoomClick,
                        eventSink = eventSink,
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RoomListSearchContentPreview(@PreviewParameter(RoomListSearchStatePreviewParam::class) state: RoomListSearchState) = ElementPreview {
    RoomListSearchContent(
        state = state,
        hideInvitesAvatars = false,
        onRoomClick = {},
        eventSink = {},
    )
}
