/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.joinroom.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import com.echo.annotations.ContributesNode
import com.echo.features.invite.api.InviteData
import com.echo.features.invite.api.acceptdecline.AcceptDeclineInviteView
import com.echo.features.invite.api.declineandblock.DeclineInviteAndBlockEntryPoint
import com.echo.features.joinroom.api.JoinRoomEntryPoint
import com.echo.libraries.architecture.BackstackView
import com.echo.libraries.architecture.BaseFlowNode
import com.echo.libraries.architecture.inputs
import com.echo.libraries.di.SessionScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class JoinRoomFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: JoinRoomPresenter.Factory,
    private val acceptDeclineInviteView: AcceptDeclineInviteView,
    private val declineAndBlockEntryPoint: DeclineInviteAndBlockEntryPoint
) : BaseFlowNode<JoinRoomFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    private val inputs: JoinRoomEntryPoint.Inputs = inputs()
    private val presenter = presenterFactory.create(
        inputs.roomId,
        inputs.roomIdOrAlias,
        inputs.roomDescription,
        inputs.serverNames,
        inputs.trigger,
    )

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class DeclineInviteAndBlockUser(val inviteData: InviteData) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.DeclineInviteAndBlockUser -> declineAndBlockEntryPoint.createNode(
                parentNode = this,
                buildContext = buildContext,
                inviteData = navTarget.inviteData,
                callback = object : DeclineInviteAndBlockEntryPoint.Callback {
                    override fun onDeclineSuccess() = navigateUp()
                },
            )
            NavTarget.Root -> rootNode(buildContext)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(modifier)
    }

    private fun rootNode(buildContext: BuildContext): Node {
        return node(buildContext) { modifier ->
            val state = presenter.present()
            JoinRoomView(
                state = state,
                onBackClick = ::navigateUp,
                onJoinSuccess = {},
                onForgetSuccess = ::navigateUp,
                onCancelKnockSuccess = {},
                onKnockSuccess = {},
                onDeclineInviteAndBlockUser = {
                    backstack.push(
                        NavTarget.DeclineInviteAndBlockUser(it)
                    )
                },
                modifier = modifier
            )
            acceptDeclineInviteView.Render(
                state = state.acceptDeclineInviteState,
                onAcceptInviteSuccess = {},
                onDeclineInviteSuccess = { navigateUp() },
                modifier = Modifier
            )
        }
    }
}
