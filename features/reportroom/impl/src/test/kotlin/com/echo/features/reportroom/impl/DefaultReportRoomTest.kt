/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.reportroom.impl

import com.google.common.truth.Truth.assertThat
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.room.FakeBaseRoom
import com.echo.tests.testutils.lambda.assert
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultReportRoomTest {
    private val roomId = A_ROOM_ID
    private val successLeaveRoomLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
    private val successReportRoomLambda =
        lambdaRecorder<String?, Result<Unit>> { _ -> Result.success(Unit) }

    private val failureLeaveRoomLambda =
        lambdaRecorder<Result<Unit>> { Result.failure(Exception("Leave room error")) }
    private val failureReportRoomLambda =
        lambdaRecorder<String?, Result<Unit>> { _ -> Result.failure(Exception("Report room error")) }

    @Test
    fun `report room, leave=false, report=false, nothing is called`() = runTest {
        val room = FakeBaseRoom(
            roomId = roomId,
            leaveRoomLambda = successLeaveRoomLambda,
            reportRoomResult = successReportRoomLambda
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(roomId, room)
        }
        val reportRoom = DefaultReportRoom(client = client)

        val result = reportRoom(roomId, shouldReport = false, reason = "", shouldLeave = false)

        assertThat(result.isSuccess).isTrue()
        assert(successLeaveRoomLambda).isNeverCalled()
        assert(successReportRoomLambda).isNeverCalled()
    }

    @Test
    fun `report room, leave=false, report=true, report room success`() = runTest {
        val room = FakeBaseRoom(
            roomId = roomId,
            leaveRoomLambda = successLeaveRoomLambda,
            reportRoomResult = successReportRoomLambda
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(roomId, room)
        }
        val reportRoom = DefaultReportRoom(client = client)

        val result = reportRoom(roomId, shouldReport = true, reason = "Spam", shouldLeave = false)

        assertThat(result.isSuccess).isTrue()
        assert(successLeaveRoomLambda).isNeverCalled()
        assert(successReportRoomLambda)
            .isCalledOnce()
            .with(value("Spam"))
    }

    @Test
    fun `report room, leave=true, report=false, leave room success`() = runTest {
        val room = FakeBaseRoom(
            roomId = roomId,
            leaveRoomLambda = successLeaveRoomLambda,
            reportRoomResult = successReportRoomLambda
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(roomId, room)
        }
        val reportRoom = DefaultReportRoom(client = client)

        val result = reportRoom(roomId, shouldReport = false, reason = "", shouldLeave = true)

        assertThat(result.isSuccess).isTrue()
        assert(successLeaveRoomLambda).isCalledOnce()
        assert(successReportRoomLambda).isNeverCalled()
    }

    @Test
    fun `report room, leave=true, report=true, leave room success`() = runTest {
        val room = FakeBaseRoom(
            roomId = roomId,
            leaveRoomLambda = successLeaveRoomLambda,
            reportRoomResult = successReportRoomLambda
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(roomId, room)
        }
        val reportRoom = DefaultReportRoom(client = client)

        val result = reportRoom(roomId, shouldReport = true, reason = "Spam", shouldLeave = true)

        assertThat(result.isSuccess).isTrue()
        assert(successLeaveRoomLambda).isCalledOnce()
        assert(successReportRoomLambda)
            .isCalledOnce()
            .with(value("Spam"))
    }

    @Test
    fun `report room, leave=true, report=true, leave room failed`() = runTest {
        val room = FakeBaseRoom(
            roomId = roomId,
            leaveRoomLambda = failureLeaveRoomLambda,
            reportRoomResult = successReportRoomLambda
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(roomId, room)
        }
        val reportRoom = DefaultReportRoom(client = client)

        val result = reportRoom(roomId, shouldReport = true, reason = "Spam", shouldLeave = true)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(ReportRoom.Exception.LeftRoomFailed)
        assert(failureLeaveRoomLambda).isCalledOnce()
        assert(successReportRoomLambda).isCalledOnce()
    }

    @Test
    fun `report room, leave=true, report=true, report room failed`() = runTest {
        val room = FakeBaseRoom(
            roomId = roomId,
            leaveRoomLambda = successLeaveRoomLambda,
            reportRoomResult = failureReportRoomLambda
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(roomId, room)
        }
        val reportRoom = DefaultReportRoom(client = client)

        val result = reportRoom(roomId, shouldReport = true, reason = "Spam", shouldLeave = true)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(ReportRoom.Exception.ReportRoomFailed)
        assert(successLeaveRoomLambda).isNeverCalled()
        assert(failureReportRoomLambda).isCalledOnce()
    }
}

