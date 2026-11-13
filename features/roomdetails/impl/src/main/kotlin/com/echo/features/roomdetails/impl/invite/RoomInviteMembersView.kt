/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.roomdetails.impl.invite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.compound.theme.ElementTheme
import com.echo.features.invitepeople.api.InvitePeopleEvent
import com.echo.features.invitepeople.api.InvitePeopleState
import com.echo.features.invitepeople.api.InvitePeopleStatePreviewParam
import com.echo.features.roomdetails.impl.R
import com.echo.libraries.designsystem.components.ProgressDialog
import com.echo.libraries.designsystem.components.button.BackButton
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.Scaffold
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.designsystem.theme.components.TextButton
import com.echo.libraries.designsystem.theme.components.TopAppBar
import com.echo.libraries.ui.strings.CommonStrings

@Composable
fun RoomInviteMembersView(
    state: InvitePeopleState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    invitePeopleView: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            RoomInviteMembersTopBar(
                onBackClick = {
                    if (state.isSearchActive) {
                        state.eventSink(InvitePeopleEvent.CloseSearch)
                    } else {
                        onBackClick()
                    }
                },
                onSubmitClick = {
                    state.eventSink(InvitePeopleEvent.SendInvites)
                },
                canSend = state.canInvite,
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            invitePeopleView()
        }
    }

    if (state.sendInvitesAction.isLoading()) {
        InviteProgressDialog()
    }
}

@Composable
private fun RoomInviteMembersTopBar(
    canSend: Boolean,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
) {
    TopAppBar(
        titleStr = stringResource(R.string.screen_room_details_invite_people_title),
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_invite),
                onClick = onSubmitClick,
                enabled = canSend,
            )
        }
    )
}

@Composable
private fun InviteProgressDialog() {
    ProgressDialog {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.screen_room_details_invite_people_preparing),
            color = ElementTheme.colors.textPrimary,
            style = ElementTheme.typography.fontHeadingSmMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.screen_room_details_invite_people_dont_close),
            color = ElementTheme.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomInviteMembersViewPreview(@PreviewParameter(InvitePeopleStatePreviewParam::class) state: InvitePeopleState) = ElementPreview {
    RoomInviteMembersView(
        state = state,
        invitePeopleView = {},
        onBackClick = {},
    )
}
