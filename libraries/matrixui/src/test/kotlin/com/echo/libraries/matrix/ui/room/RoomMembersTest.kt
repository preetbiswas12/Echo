/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.ui.room

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.echo.libraries.matrix.api.room.RoomMembersState
import com.echo.libraries.matrix.api.room.RoomMembershipState
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.A_USER_ID_2
import com.echo.libraries.matrix.test.A_USER_ID_3
import com.echo.libraries.matrix.test.room.FakeBaseRoom
import com.echo.libraries.matrix.test.room.aRoomInfo
import com.echo.libraries.matrix.test.room.aRoomMember
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomMembersTest {
    private val roomMember1 = aRoomMember(A_USER_ID)
    private val roomMember2 = aRoomMember(A_USER_ID_2)
    private val roomMember3 = aRoomMember(A_USER_ID_3)

    @Test
    fun `getDirectRoomMember emits other member for encrypted DM`() = runTest {
        val joinedRoom = FakeBaseRoom(
            sessionId = A_USER_ID,
            initialRoomInfo = aRoomInfo(isDm = true, isEncrypted = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            joinedRoom.getDirectRoomMember(
                RoomMembersState.Ready(persistentListOf(roomMember1, roomMember2))
            )
        }.test {
            assertThat(awaitItem().value).isEqualTo(roomMember2)
        }
    }

    @Test
    fun `getDirectRoomMember emit null if the room is not a dm`() = runTest {
        val joinedRoom = FakeBaseRoom(
            sessionId = A_USER_ID,
            initialRoomInfo = aRoomInfo(isDm = false)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            joinedRoom.getDirectRoomMember(
                RoomMembersState.Ready(persistentListOf(roomMember1, roomMember2))
            )
        }.test {
            assertThat(awaitItem().value).isNull()
        }
    }

    @Test
    fun `getDirectRoomMember emits other member even if the room is not encrypted`() = runTest {
        val joinedRoom = FakeBaseRoom(
            sessionId = A_USER_ID,
            initialRoomInfo = aRoomInfo(isDm = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            joinedRoom.getDirectRoomMember(
                RoomMembersState.Ready(persistentListOf(roomMember1, roomMember2))
            )
        }.test {
            assertThat(awaitItem().value).isEqualTo(roomMember2)
        }
    }

    @Test
    fun `getDirectRoomMember emit null if the other member is not active`() = runTest {
        val joinedRoom = FakeBaseRoom(
            sessionId = A_USER_ID,
            initialRoomInfo = aRoomInfo(isDm = true),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            joinedRoom.getDirectRoomMember(
                RoomMembersState.Ready(
                    persistentListOf(
                        roomMember1,
                        roomMember2.copy(membership = RoomMembershipState.BAN),
                    )
                )
            )
        }.test {
            assertThat(awaitItem().value).isNull()
        }
    }

    @Test
    fun `getDirectRoomMember emit the other member if there are 2 active members`() = runTest {
        val joinedRoom = FakeBaseRoom(
            sessionId = A_USER_ID,
            initialRoomInfo = aRoomInfo(isDm = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            joinedRoom.getDirectRoomMember(
                RoomMembersState.Ready(
                    persistentListOf(
                        roomMember1,
                        roomMember2,
                        roomMember3.copy(membership = RoomMembershipState.BAN),
                    )
                )
            )
        }.test {
            assertThat(awaitItem().value).isEqualTo(roomMember2)
        }
    }

    @Test
    fun `getCurrentRoomMember returns the current user`() = runTest {
        val joinedRoom = FakeBaseRoom(sessionId = A_USER_ID)
        moleculeFlow(RecompositionMode.Immediate) {
            joinedRoom.getCurrentRoomMember(
                RoomMembersState.Ready(
                    persistentListOf(
                        roomMember1,
                        roomMember2,
                        roomMember3,
                    )
                )
            )
        }.test {
            assertThat(awaitItem().value).isEqualTo(roomMember1)
        }
    }

    @Test
    fun `getCurrentRoomMember returns null if the member is not found`() = runTest {
        val joinedRoom = FakeBaseRoom(sessionId = A_USER_ID)
        moleculeFlow(RecompositionMode.Immediate) {
            joinedRoom.getCurrentRoomMember(
                RoomMembersState.Ready(
                    persistentListOf(
                        roomMember2,
                        roomMember3,
                    )
                )
            )
        }.test {
            assertThat(awaitItem().value).isNull()
        }
    }
}
