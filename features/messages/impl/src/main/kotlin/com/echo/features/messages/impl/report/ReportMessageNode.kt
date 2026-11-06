/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.report

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
import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.UserId

@ContributesNode(RoomScope::class)
@AssistedInject
class ReportMessageNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ReportMessagePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val eventId: EventId,
        val senderId: UserId,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()

    private val presenter = presenterFactory.create(
        ReportMessagePresenter.Inputs(inputs.eventId, inputs.senderId)
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ReportMessageView(
            state = state,
            onBackClick = ::navigateUp,
            modifier = modifier
        )
    }
}

