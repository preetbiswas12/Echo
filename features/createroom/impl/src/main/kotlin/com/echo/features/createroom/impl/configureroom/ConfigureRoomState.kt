/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.createroom.impl.configureroom

import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.spaces.SpaceRoom
import com.echo.libraries.matrix.ui.media.AvatarAction
import com.echo.libraries.matrix.ui.room.address.RoomAddressValidity
import com.echo.libraries.permissions.api.PermissionsState
import kotlinx.collections.immutable.ImmutableList

data class ConfigureRoomState(
    val isSpace: Boolean,
    val config: CreateRoomConfig,
    val avatarActions: ImmutableList<AvatarAction>,
    val createRoomAction: AsyncAction<RoomId>,
    val cameraPermissionState: PermissionsState,
    val roomAddressValidity: RoomAddressValidity,
    val homeserverName: String,
    val availableJoinRules: ImmutableList<JoinRuleItem>,
    val spaces: ImmutableList<SpaceRoom>,
    val eventSink: (ConfigureRoomEvent) -> Unit
) {
    val isValid: Boolean = config.roomName?.isNotEmpty() == true &&
        (config.visibilityState is RoomVisibilityState.Private || roomAddressValidity == RoomAddressValidity.Valid) &&
        config.visibilityState.joinRuleItem in availableJoinRules
}

