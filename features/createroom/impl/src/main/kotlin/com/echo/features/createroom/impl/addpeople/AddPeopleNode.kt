/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.createroom.impl.addpeople

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.echo.annotations.ContributesNode
import com.echo.features.invitepeople.api.InvitePeoplePresenter
import com.echo.features.invitepeople.api.InvitePeopleRenderer
import com.echo.libraries.architecture.NodeInputs
import com.echo.libraries.architecture.callback
import com.echo.libraries.architecture.inputs
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.core.RoomId
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@ContributesNode(SessionScope::class)
@AssistedInject
class AddPeopleNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    invitePeoplePresenterFactory: InvitePeoplePresenter.Factory,
    private val invitePeopleRenderer: InvitePeopleRenderer,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val roomId: RoomId,
    ) : NodeInputs

    interface Callback : Plugin {
        fun onFinish()
    }

    private val callback: Callback = callback()
    private val roomId = inputs<Inputs>().roomId
    private val invitePeoplePresenter = invitePeoplePresenterFactory.create(
        joinedRoom = null,
        roomId = roomId,
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = invitePeoplePresenter.present()
        AddPeopleView(
            state = state,
            onFinish = callback::onFinish,
        ) {
            invitePeopleRenderer.Render(state, Modifier)
        }
    }
}
