/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.reportroom.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.libraries.architecture.NodeInputs
import com.echo.libraries.architecture.inputs
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.core.RoomId

@ContributesNode(SessionScope::class)
@AssistedInject
class ReportRoomNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ReportRoomPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(val roomId: RoomId) : NodeInputs

    private val roomId = inputs<Inputs>().roomId
    private val presenter: ReportRoomPresenter = presenterFactory.create(roomId = roomId)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ReportRoomView(
            state = state,
            modifier = modifier,
            onBackClick = ::navigateUp,
        )
    }
}

