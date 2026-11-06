/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.joinroom.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import im.vector.app.features.analytics.plan.JoinedRoom
import com.echo.features.roomdirectory.api.RoomDescription
import com.echo.libraries.architecture.FeatureEntryPoint
import com.echo.libraries.architecture.NodeInputs
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.RoomIdOrAlias
import java.util.Optional

interface JoinRoomEntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: Inputs,
    ): Node

    data class Inputs(
        val roomId: RoomId,
        val roomIdOrAlias: RoomIdOrAlias,
        val roomDescription: Optional<RoomDescription>,
        val serverNames: List<String>,
        val trigger: JoinedRoom.Trigger,
    ) : NodeInputs
}

