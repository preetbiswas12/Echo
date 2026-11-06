/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.services.appnavstate.test

import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.services.appnavstate.api.ActiveRoomsHolder

class FakeActiveRoomsHolder : ActiveRoomsHolder {
    private var room: JoinedRoom? = null

    override fun addRoom(room: JoinedRoom) {
        this.room = room
    }

    override fun getActiveRoom(sessionId: SessionId): JoinedRoom? {
        return room
    }

    override fun getActiveRoomMatching(sessionId: SessionId, roomId: RoomId): JoinedRoom? {
        return null
    }

    override fun removeRoom(sessionId: SessionId, roomId: RoomId) {
        room = null
    }

    override fun clear(sessionId: SessionId) {
    }
}

