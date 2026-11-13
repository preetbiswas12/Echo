/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.x.di

import com.echo.appnav.di.RoomGraphFactory
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.room.JoinedRoom
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(SessionScope::class)
class DefaultRoomGraphFactory(
    private val sessionGraph: SessionGraph,
) : RoomGraphFactory {
    override fun create(room: JoinedRoom): Any {
        return sessionGraph.roomGraphFactory
            .create(room, room)
    }
}
