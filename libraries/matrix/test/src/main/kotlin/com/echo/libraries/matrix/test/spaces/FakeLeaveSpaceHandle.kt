/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test.spaces

import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.spaces.LeaveSpaceHandle
import com.echo.libraries.matrix.api.spaces.LeaveSpaceRoom
import com.echo.libraries.matrix.test.A_SPACE_ID
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask

class FakeLeaveSpaceHandle(
    override val id: RoomId = A_SPACE_ID,
    private val roomsResult: () -> Result<List<LeaveSpaceRoom>> = { lambdaError() },
    private val leaveResult: (List<RoomId>) -> Result<Unit> = { lambdaError() },
    private val closeResult: () -> Unit = { lambdaError() },
) : LeaveSpaceHandle {
    override suspend fun rooms(): Result<List<LeaveSpaceRoom>> = simulateLongTask {
        roomsResult()
    }

    override suspend fun leave(roomIds: List<RoomId>): Result<Unit> = simulateLongTask {
        leaveResult(roomIds)
    }

    override fun close() {
        return closeResult()
    }
}

