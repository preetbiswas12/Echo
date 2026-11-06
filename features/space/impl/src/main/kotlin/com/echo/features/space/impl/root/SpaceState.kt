/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.space.impl.root

import androidx.compose.runtime.Immutable
import com.echo.features.invite.api.acceptdecline.AcceptDeclineInviteState
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.room.RoomInfo
import com.echo.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet

data class SpaceState(
    val spaceInfo: RoomInfo,
    val children: ImmutableList<SpaceRoom>,
    val seenSpaceInvites: ImmutableSet<RoomId>,
    val hideInvitesAvatar: Boolean,
    val hasMoreToLoad: Boolean,
    val joinActions: ImmutableMap<RoomId, AsyncAction<Unit>>,
    val acceptDeclineInviteState: AcceptDeclineInviteState,
    val topicViewerState: TopicViewerState,
    val canAccessSpaceSettings: Boolean,
    val isManageMode: Boolean,
    val selectedRoomIds: ImmutableSet<RoomId>,
    val canEditSpaceGraph: Boolean,
    val removeRoomsAction: AsyncAction<Unit>,
    val eventSink: (SpaceEvent) -> Unit
) {
    fun isJoining(spaceId: RoomId): Boolean = joinActions[spaceId] == AsyncAction.Loading
    fun isSelected(spaceId: RoomId): Boolean = selectedRoomIds.contains(spaceId)
    val hasAnyJoinFailures: Boolean = joinActions.values.any {
        it is AsyncAction.Failure
    }

    val showManageRoomsAction: Boolean = canEditSpaceGraph && children.any { spaceRoom -> !spaceRoom.isSpace }
    val selectedCount: Int = selectedRoomIds.size
    val isRemoveButtonEnabled: Boolean = selectedRoomIds.isNotEmpty()
}

@Immutable
sealed interface TopicViewerState {
    data object Hidden : TopicViewerState
    data class Shown(val topic: String) : TopicViewerState
}

