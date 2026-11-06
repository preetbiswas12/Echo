/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.rolesandpermissions.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.echo.features.rolesandpermissions.api.ChangeRoomMemberRolesEntryPoint
import com.echo.features.rolesandpermissions.api.ChangeRoomMemberRolesListType
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.tests.testutils.lambda.lambdaError

class FakeChangeRoomMemberRolesEntryPoint : ChangeRoomMemberRolesEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        room: JoinedRoom,
        listType: ChangeRoomMemberRolesListType,
    ): Node {
        lambdaError()
    }
}

