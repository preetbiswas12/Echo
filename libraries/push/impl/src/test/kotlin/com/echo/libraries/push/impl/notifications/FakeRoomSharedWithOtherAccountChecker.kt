/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.notifications

import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId

class FakeRoomSharedWithOtherAccountChecker(
    private val isJoinedByOtherAccountResult: (SessionId, RoomId) -> Boolean = { _, _ -> false },
) : RoomSharedWithOtherAccountChecker {
    override suspend fun isJoinedByOtherAccount(sessionId: SessionId, roomId: RoomId): Boolean {
        return isJoinedByOtherAccountResult(sessionId, roomId)
    }
}
