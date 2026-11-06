/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.roomdetails.impl.invite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import com.echo.annotations.ContributesNode
import com.echo.features.invitepeople.api.InvitePeopleEvent
import com.echo.features.invitepeople.api.InvitePeoplePresenter
import com.echo.features.invitepeople.api.InvitePeopleRenderer
import com.echo.libraries.architecture.callback
import com.echo.libraries.designsystem.components.ProgressDialog
import com.echo.libraries.designsystem.components.async.AsyncActionView
import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.services.analytics.api.AnalyticsService

@ContributesNode(RoomScope::class)
@AssistedInject
class RoomInviteMembersNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val analyticsService: AnalyticsService,
    private val invitePeopleRenderer: InvitePeopleRenderer,
    room: JoinedRoom,
    invitePeoplePresenterFactory: InvitePeoplePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun openCreatedRoom(roomId: RoomId)
    }

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.Invites))
            }
        )
    }

    private val invitePeoplePresenter = invitePeoplePresenterFactory.create(
        joinedRoom = room,
        roomId = room.roomId,
    )

    private val callback = plugins.callback<Callback>()

    @Composable
    override fun View(modifier: Modifier) {
        val state = invitePeoplePresenter.present()

        // Once invites have been sent successfully, close the Invite view.
        LaunchedEffect(state.sendInvitesAction) {
            if (state.sendInvitesAction.isReady()) {
                navigateUp()
            }
        }

        AsyncActionView(
            async = state.createRoomFromDmAction,
            onSuccess = { roomId ->
                callback.openCreatedRoom(roomId)
            },
            progressDialog = {
                ProgressDialog(text = stringResource(CommonStrings.common_creating_room))
            },
            onErrorDismiss = {
                state.eventSink(InvitePeopleEvent.ClearError)
            }
        )

        RoomInviteMembersView(
            state = state,
            modifier = modifier,
            onBackClick = { navigateUp() }
        ) {
            invitePeopleRenderer.Render(state, Modifier)
        }
    }
}

