/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.startchat.impl.root

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.echo.annotations.ContributesNode
import com.echo.features.startchat.impl.StartChatNavigator
import com.echo.libraries.deeplink.api.usecase.InviteFriendsUseCase
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.core.toRoomIdOrAlias
import com.echo.services.analytics.api.AnalyticsService
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen

@ContributesNode(SessionScope::class)
@AssistedInject
class StartChatNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: StartChatPresenter,
    private val analyticsService: AnalyticsService,
    private val inviteFriendsUseCase: InviteFriendsUseCase,
) : Node(buildContext, plugins = plugins) {
    private val navigator = plugins<StartChatNavigator>().first()

    init {
        lifecycle.subscribe(
            onResume = { analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.StartChat)) }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = requireNotNull(LocalActivity.current)
        StartChatView(
            state = state,
            modifier = modifier,
            onCloseClick = this::navigateUp,
            onNewRoomClick = navigator::onCreateNewRoom,
            onOpenDM = {
                navigator.onRoomCreated(roomIdOrAlias = it.toRoomIdOrAlias(), serverNames = emptyList())
            },
            onJoinByAddressClick = navigator::onShowJoinRoomByAddress,
            onInviteFriendsClick = { invitePeople(activity) },
            onRoomDirectorySearchClick = navigator::onOpenRoomDirectory
        )
    }

    private fun invitePeople(activity: Activity) {
        inviteFriendsUseCase.execute(activity)
    }
}
