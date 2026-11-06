/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.developer.appsettings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.features.preferences.api.ExtraDeveloperOptionsRenderer
import com.echo.features.preferences.api.PreferencesEntryPoint
import com.echo.libraries.architecture.callback

@ContributesNode(AppScope::class)
@AssistedInject
class AppDeveloperSettingsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: AppDeveloperSettingsPresenter,
    private val extraDeveloperOptionsRenderer: ExtraDeveloperOptionsRenderer,
) : Node(buildContext, plugins = plugins) {
    private val callback: PreferencesEntryPoint.DeveloperSettingsCallback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        AppDeveloperSettingsPage(
            state = state,
            modifier = modifier,
            onBackClick = callback::onDone,
            extraOptions = { extraDeveloperOptionsRenderer.Render(Modifier) },
        )
    }
}

