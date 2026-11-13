/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.knockrequests.impl.banner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.echo.features.knockrequests.api.banner.KnockRequestsBannerRenderer
import com.echo.libraries.di.RoomScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(RoomScope::class)
class DefaultKnockRequestsBannerRenderer(
    private val presenter: KnockRequestsBannerPresenter,
) : KnockRequestsBannerRenderer {
    @Composable
    override fun View(modifier: Modifier, onViewRequestsClick: () -> Unit) {
        val state = presenter.present()
        KnockRequestsBannerView(
            state = state,
            onViewRequestsClick = onViewRequestsClick,
        )
    }
}
