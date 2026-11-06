/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.verifysession.impl.incoming

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.features.verifysession.api.IncomingVerificationEntryPoint
import com.echo.libraries.architecture.callback
import com.echo.libraries.architecture.inputs
import com.echo.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class IncomingVerificationNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: IncomingVerificationPresenter.Factory,
) : Node(buildContext, plugins = plugins),
    IncomingVerificationNavigator {
    private val callback: IncomingVerificationEntryPoint.Callback = callback()
    private val presenter = presenterFactory.create(
        verificationRequest = inputs<IncomingVerificationEntryPoint.Params>().verificationRequest,
        navigator = this,
    )

    override fun onFinish() {
        callback.onDone()
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        IncomingVerificationView(
            state = state,
            modifier = modifier,
        )
    }
}

