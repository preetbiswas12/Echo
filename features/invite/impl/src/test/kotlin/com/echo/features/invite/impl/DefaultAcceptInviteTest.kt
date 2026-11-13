/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invite.impl

import com.echo.features.invite.test.InMemorySeenInvitesStore
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.RoomIdOrAlias
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.toRoomIdOrAlias
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.room.join.FakeJoinRoom
import com.echo.libraries.push.test.notifications.FakeNotificationCleaner
import com.echo.tests.testutils.lambda.any
import com.echo.tests.testutils.lambda.assert
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAcceptInviteTest {
    private val roomId = A_ROOM_ID
    private val client = FakeMatrixClient()
    private val seenInvitesStore = InMemorySeenInvitesStore(initialRoomIds = setOf(roomId))

    private val clearMembershipNotificationForRoomLambda =
        lambdaRecorder<SessionId, RoomId, Unit> { _, _ -> }
    private val notificationCleaner =
        FakeNotificationCleaner(clearMembershipNotificationForRoomLambda = clearMembershipNotificationForRoomLambda)

    @Test
    fun `accept invite success scenario`() = runTest {
        val joinRoomLambda =
            lambdaRecorder<RoomIdOrAlias, List<String>, JoinedRoom.Trigger, Result<Unit>> { _, _, _ ->
                Result.success(Unit)
            }

        val acceptInvite = DefaultAcceptInvite(
            client = client,
            notificationCleaner = notificationCleaner,
            joinRoom = FakeJoinRoom(lambda = joinRoomLambda),
            seenInvitesStore = seenInvitesStore
        )

        val result = acceptInvite(roomId)

        assertThat(result.isSuccess).isTrue()

        assert(joinRoomLambda)
            .isCalledOnce()
            .with(value(roomId.toRoomIdOrAlias()), any(), any())

        assert(clearMembershipNotificationForRoomLambda)
            .isCalledOnce()
            .with(value(client.sessionId), value(roomId))

        assertThat(seenInvitesStore.seenRoomIds().first()).isEmpty()
    }

    @Test
    fun `accept invite failure scenario`() = runTest {
        val joinRoomLambda =
            lambdaRecorder<RoomIdOrAlias, List<String>, JoinedRoom.Trigger, Result<Unit>> { _, _, _ ->
                Result.failure(RuntimeException("Join room failed"))
            }

        val acceptInvite = DefaultAcceptInvite(
            client = client,
            notificationCleaner = notificationCleaner,
            joinRoom = FakeJoinRoom(lambda = joinRoomLambda),
            seenInvitesStore = seenInvitesStore
        )

        val result = acceptInvite(roomId)

        assertThat(result.isFailure).isTrue()

        assert(joinRoomLambda)
            .isCalledOnce()
            .with(value(roomId.toRoomIdOrAlias()), any(), any())

        assert(clearMembershipNotificationForRoomLambda).isNeverCalled()

        assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(roomId)
    }
}
