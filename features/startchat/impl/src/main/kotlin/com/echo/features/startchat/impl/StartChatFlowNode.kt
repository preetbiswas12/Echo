/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.startchat.impl

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.transition.JumpToEndTransitionHandler
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.features.createroom.api.CreateRoomEntryPoint
import com.echo.features.startchat.api.StartChatEntryPoint
import com.echo.features.startchat.impl.joinbyaddress.JoinRoomByAddressNode
import com.echo.features.startchat.impl.root.StartChatNode
import com.echo.libraries.architecture.BackstackView
import com.echo.libraries.architecture.BaseFlowNode
import com.echo.libraries.architecture.OverlayView
import com.echo.libraries.architecture.callback
import com.echo.libraries.architecture.createNode
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.toRoomIdOrAlias
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class StartChatFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val createRoomEntryPoint: CreateRoomEntryPoint,
) : BaseFlowNode<StartChatFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object NewRoom : NavTarget

        @Parcelize
        data object JoinByAddress : NavTarget
    }

    private val callback: StartChatEntryPoint.Callback = callback()
    private val navigator = DefaultStartChatNavigator(
        backstack = backstack,
        overlay = overlay,
        openRoom = callback::onRoomCreated,
        openRoomDirectory = callback::navigateToRoomDirectory,
    )

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                createNode<StartChatNode>(buildContext = buildContext, plugins = listOf(navigator))
            }
            NavTarget.NewRoom -> {
                val callback = object : CreateRoomEntryPoint.Callback {
                    override fun onRoomCreated(roomId: RoomId) {
                        navigator.onRoomCreated(roomId.toRoomIdOrAlias(), emptyList())
                    }
                }
                createRoomEntryPoint
                    .builder(
                        parentNode = this,
                        buildContext = buildContext,
                        callback = callback,
                    )
                    .setIsSpace(false)
                    .build()
            }
            NavTarget.JoinByAddress -> {
                createNode<JoinRoomByAddressNode>(buildContext = buildContext, plugins = listOf(navigator))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Box(modifier = modifier) {
            BackstackView()
            OverlayView(transitionHandler = remember { JumpToEndTransitionHandler() })
        }
    }
}

