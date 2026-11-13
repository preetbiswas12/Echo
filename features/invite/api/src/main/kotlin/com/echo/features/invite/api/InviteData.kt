/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invite.api

import android.os.Parcelable
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.room.RoomInfo
import com.echo.libraries.matrix.api.room.preview.RoomPreviewInfo
import com.echo.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.parcelize.Parcelize

@Parcelize
data class InviteData(
    val roomId: RoomId,
    val roomName: String,
    val isDm: Boolean,
) : Parcelable

fun RoomPreviewInfo.toInviteData(): InviteData {
    return InviteData(
        roomId = roomId,
        roomName = name ?: roomId.value,
        isDm = false,
    )
}

fun RoomInfo.toInviteData(): InviteData {
    return InviteData(
        roomId = id,
        roomName = name ?: id.value,
        isDm = isDm,
    )
}

fun SpaceRoom.toInviteData(): InviteData {
    return InviteData(
        roomId = roomId,
        roomName = displayName,
        isDm = false,
    )
}
