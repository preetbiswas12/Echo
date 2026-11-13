/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test.room

import com.echo.libraries.matrix.api.room.BaseRoom
import com.echo.libraries.matrix.api.room.NotJoinedRoom
import com.echo.libraries.matrix.api.room.RoomMembershipDetails
import com.echo.libraries.matrix.api.room.preview.RoomPreviewInfo
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask

class FakeNotJoinedRoom(
    override val localRoom: BaseRoom? = null,
    override val previewInfo: RoomPreviewInfo = aRoomPreviewInfo(),
    private val roomMembershipDetails: () -> Result<RoomMembershipDetails?> = { lambdaError() },
) : NotJoinedRoom {
    override suspend fun membershipDetails(): Result<RoomMembershipDetails?> = simulateLongTask {
        roomMembershipDetails()
    }

    override fun close() = Unit
}
