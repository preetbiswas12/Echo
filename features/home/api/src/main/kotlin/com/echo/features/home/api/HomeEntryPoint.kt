/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.echo.libraries.architecture.FeatureEntryPoint
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.room.JoinedRoom

interface HomeEntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        fun navigateToRoom(roomId: RoomId, eventId: EventId?, joinedRoom: JoinedRoom?)
        fun navigateToCreateRoom()
        fun navigateToCreateSpace()
        fun navigateToSettings()
        fun navigateToSetUpRecovery()
        fun navigateToEnterRecoveryKey()
        fun navigateToRoomSettings(roomId: RoomId)
        fun navigateToBugReport()
        fun navigateToAddAccount()
        fun navigateToSession(sessionId: SessionId)
    }
}

