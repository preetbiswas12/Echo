/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.signedout.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.libraries.architecture.NodeInputs
import com.echo.libraries.architecture.inputs
import com.echo.libraries.matrix.api.core.SessionId

@ContributesNode(AppScope::class)
@AssistedInject
class SignedOutNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SignedOutPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val sessionId: SessionId,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(inputs.sessionId)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        SignedOutView(
            state = state,
            modifier = modifier
        )
    }
}

