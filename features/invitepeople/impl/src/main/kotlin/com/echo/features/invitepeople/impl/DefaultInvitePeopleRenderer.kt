/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invitepeople.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.echo.features.invitepeople.api.InvitePeopleRenderer
import com.echo.features.invitepeople.api.InvitePeopleState
import com.echo.libraries.di.SessionScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(SessionScope::class)
class DefaultInvitePeopleRenderer : InvitePeopleRenderer {
    @Composable
    override fun Render(state: InvitePeopleState, modifier: Modifier) {
        if (state is DefaultInvitePeopleState) {
            InvitePeopleView(
                state = state,
                modifier = modifier
            )
        } else {
            error("Unsupported state type: ${state::javaClass}")
        }
    }
}
