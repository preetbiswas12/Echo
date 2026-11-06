/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.libraries.designsystem.components.avatar.AvatarData
import com.echo.libraries.designsystem.components.avatar.AvatarSize
import com.echo.libraries.matrix.api.spaces.SpaceRoom
import com.echo.libraries.matrix.api.spaces.SpaceRoomVisibility
import com.echo.libraries.ui.strings.CommonStrings

fun SpaceRoom.getAvatarData(size: AvatarSize) = AvatarData(
    id = roomId.value,
    name = displayName,
    url = avatarUrl,
    size = size,
)

val SpaceRoomVisibility.icon: ImageVector
    @Composable
    get() {
        return when (this) {
            SpaceRoomVisibility.Private -> CompoundIcons.LockSolid()
            SpaceRoomVisibility.Public -> CompoundIcons.Public()
            SpaceRoomVisibility.SpaceMembers -> CompoundIcons.Space()
        }
    }

val SpaceRoomVisibility.label: String
    @Composable
    @ReadOnlyComposable
    get() {
        return when (this) {
            SpaceRoomVisibility.Private -> stringResource(CommonStrings.common_private)
            SpaceRoomVisibility.Public -> stringResource(CommonStrings.common_public)
            SpaceRoomVisibility.SpaceMembers -> stringResource(CommonStrings.common_space_members)
        }
    }

