/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.poll.impl.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.echo.annotations.ContributesNode
import com.echo.libraries.architecture.callback
import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.core.EventId
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@ContributesNode(RoomScope::class)
@AssistedInject
class PollHistoryNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PollHistoryPresenter,
) : Node(
    buildContext = buildContext,
    plugins = plugins,
) {
    interface Callback : Plugin {
        fun navigateToEditPoll(pollStartEventId: EventId)
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        PollHistoryView(
            state = presenter.present(),
            modifier = modifier,
            onEditPoll = callback::navigateToEditPoll,
            goBack = this::navigateUp,
        )
    }
}
