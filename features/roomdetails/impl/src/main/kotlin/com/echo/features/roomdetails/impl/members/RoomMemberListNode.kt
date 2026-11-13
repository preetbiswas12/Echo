/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.roomdetails.impl.members

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.echo.annotations.ContributesNode
import com.echo.features.roommembermoderation.api.ModerationAction
import com.echo.features.roommembermoderation.api.RoomMemberModerationEvent
import com.echo.features.roommembermoderation.api.RoomMemberModerationRenderer
import com.echo.libraries.architecture.appyx.launchMolecule
import com.echo.libraries.architecture.callback
import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.ui.model.getBestName
import com.echo.services.analytics.api.AnalyticsService
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen

@ContributesNode(RoomScope::class)
@AssistedInject
class RoomMemberListNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RoomMemberListPresenter,
    private val analyticsService: AnalyticsService,
    private val roomMemberModerationRenderer: RoomMemberModerationRenderer,
) : Node(buildContext, plugins = plugins), RoomMemberListNavigator {
    interface Callback : Plugin {
        fun navigateToRoomMemberDetails(roomMemberId: UserId)
        fun navigateToInviteMembers()
        fun navigateToAvatarPreview(username: String, avatarUrl: String)
    }

    private val callback: Callback = callback()
    private val stateFlow = launchMolecule { presenter.present() }

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.RoomMembers))
            }
        )
    }

    override fun openRoomMemberDetails(roomMemberId: UserId) {
        callback.navigateToRoomMemberDetails(roomMemberId)
    }

    override fun openInviteMembers() {
        callback.navigateToInviteMembers()
    }

    override fun exitRoomMemberList() {
        navigateUp()
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by stateFlow.collectAsState()
        RoomMemberListView(
            state = state,
            modifier = modifier,
            navigator = this,
        )
        roomMemberModerationRenderer.Render(
            state = state.moderationState,
            onSelectAction = { action, target ->
                when (action) {
                    is ModerationAction.DisplayProfile -> openRoomMemberDetails(target.userId)
                    else -> state.moderationState.eventSink(RoomMemberModerationEvent.ProcessAction(action, target))
                }
            },
            onAvatarClick = { user ->
                user.avatarUrl?.let { url ->
                    callback.navigateToAvatarPreview(user.getBestName(), url)
                }
            },
            modifier = Modifier,
        )
    }
}

interface RoomMemberListNavigator {
    fun exitRoomMemberList()
    fun openRoomMemberDetails(roomMemberId: UserId)
    fun openInviteMembers()
}
