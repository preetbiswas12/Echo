/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.roomdetailsedit.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.echo.features.roomdetailsedit.api.RoomDetailsEditEntryPoint
import com.echo.tests.testutils.lambda.lambdaError

class FakeRoomDetailsEditEntryPoint : RoomDetailsEditEntryPoint {
    override fun createNode(parentNode: Node, buildContext: BuildContext): Node {
        lambdaError()
    }
}

