/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.qrcode.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.features.login.impl.di.QrCodeLoginScope
import com.echo.features.login.impl.qrcode.QrCodeErrorScreenType
import com.echo.libraries.architecture.callback
import com.echo.libraries.architecture.inputs
import com.echo.libraries.core.meta.BuildMeta

@ContributesNode(QrCodeLoginScope::class)
@AssistedInject
class QrCodeErrorNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val buildMeta: BuildMeta,
) : Node(buildContext = buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onRetry()
        fun onCancel()
    }

    private val callback: Callback = callback()
    private val qrCodeErrorScreenType = inputs<QrCodeErrorScreenType>()

    @Composable
    override fun View(modifier: Modifier) {
        QrCodeErrorView(
            modifier = modifier,
            errorScreenType = qrCodeErrorScreenType,
            appName = buildMeta.productionApplicationName,
            onRetry = callback::onRetry,
            onCancel = callback::onCancel,
        )
    }
}

