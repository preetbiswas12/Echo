/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.leaveroom.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.echo.features.leaveroom.api.LeaveRoomRenderer
import com.echo.features.leaveroom.api.LeaveRoomState
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.core.RoomId
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(SessionScope::class)
class InternalLeaveRoomRenderer : LeaveRoomRenderer {
    @Composable
    override fun Render(state: LeaveRoomState, onSelectNewOwners: (RoomId) -> Unit, modifier: Modifier) {
        if (state is InternalLeaveRoomState) {
            LeaveRoomView(state, onSelectNewOwners)
        } else {
            error("Unsupported state type ${state.javaClass}")
        }
    }
}
