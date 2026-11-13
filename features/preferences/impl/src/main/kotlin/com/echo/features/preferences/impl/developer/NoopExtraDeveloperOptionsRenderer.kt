/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.developer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.echo.features.preferences.api.ExtraDeveloperOptionsRenderer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class NoopExtraDeveloperOptionsRenderer : ExtraDeveloperOptionsRenderer {
    @Composable
    override fun Render(modifier: Modifier) {
        // No-op
    }
}
