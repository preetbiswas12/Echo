/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.rolesandpermissions.impl.roles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.features.rolesandpermissions.api.ChangeRoomMemberRolesListType
import com.echo.libraries.architecture.NodeInputs
import com.echo.libraries.architecture.appyx.launchMolecule
import com.echo.libraries.architecture.inputs
import com.echo.libraries.core.bool.orFalse
import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.room.RoomMember
import kotlinx.coroutines.flow.first

@ContributesNode(RoomScope::class)
@AssistedInject
class ChangeRolesNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ChangeRolesPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val listType: ChangeRoomMemberRolesListType,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(inputs.listType.toRoomMemberRole())
    private val stateFlow = launchMolecule { presenter.present() }

    suspend fun waitForCompletion(): Boolean {
        val successState = stateFlow.first { it.savingState.isSuccess() }
        return successState.savingState.dataOrNull().orFalse()
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by stateFlow.collectAsState()
        ChangeRolesView(
            state = state,
            modifier = modifier,
        )
    }
}

internal fun ChangeRoomMemberRolesListType.toRoomMemberRole() = when (this) {
    ChangeRoomMemberRolesListType.Admins -> RoomMember.Role.Admin
    ChangeRoomMemberRolesListType.Moderators -> RoomMember.Role.Moderator
    ChangeRoomMemberRolesListType.SelectNewOwnersWhenLeaving -> RoomMember.Role.Owner(isCreator = false)
}

