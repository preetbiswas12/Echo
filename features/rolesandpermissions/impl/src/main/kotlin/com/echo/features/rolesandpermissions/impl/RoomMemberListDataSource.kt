/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.rolesandpermissions.impl

import com.echo.libraries.core.bool.orFalse
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.matrix.api.room.BaseRoom
import com.echo.libraries.matrix.api.room.RoomMember
import com.echo.libraries.matrix.api.room.roomMembers
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

@Inject
class RoomMemberListDataSource(
    private val room: BaseRoom,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    suspend fun search(query: String): List<RoomMember> = withContext(coroutineDispatchers.io) {
        val roomMembersState = room.membersStateFlow.value
        val activeRoomMembers = roomMembersState.roomMembers()
            ?.filter { it.membership.isActive() }
            .orEmpty()
        val filteredMembers = if (query.isBlank()) {
            activeRoomMembers
        } else {
            activeRoomMembers.filter { member ->
                member.userId.value.contains(query, ignoreCase = true) ||
                        member.displayName?.contains(query, ignoreCase = true).orFalse()
            }
        }
        filteredMembers
    }
}
