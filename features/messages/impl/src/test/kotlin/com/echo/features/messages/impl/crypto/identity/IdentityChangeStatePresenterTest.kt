/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.crypto.identity

import com.echo.libraries.designsystem.components.avatar.AvatarSize
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.encryption.EncryptionService
import com.echo.libraries.matrix.api.encryption.identity.IdentityState
import com.echo.libraries.matrix.api.encryption.identity.IdentityStateChange
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.libraries.matrix.api.room.RoomMembersState
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.A_USER_ID_2
import com.echo.libraries.matrix.test.encryption.FakeEncryptionService
import com.echo.libraries.matrix.test.room.FakeJoinedRoom
import com.echo.libraries.matrix.test.room.aRoomInfo
import com.echo.libraries.matrix.test.room.aRoomMember
import com.echo.tests.testutils.WarmUpRule
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import com.echo.tests.testutils.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class IdentityChangeStatePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createIdentityChangeStatePresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomMemberIdentityStateChanges).isEmpty()
        }
    }

    @Test
    fun `present - when the room emits identity change, the presenter emits new state`() = runTest {
        val identityStateChanges = MutableStateFlow(emptyList<IdentityStateChange>())
        val room = FakeJoinedRoom(identityStateChangesFlow = identityStateChanges).apply {
            givenRoomInfo(aRoomInfo(isEncrypted = true))
        }
        val presenter = createIdentityChangeStatePresenter(room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomMemberIdentityStateChanges).isEmpty()
            identityStateChanges.emit(
                listOf(
                    IdentityStateChange(
                        userId = A_USER_ID_2,
                        identityState = IdentityState.PinViolation,
                    ),
                )
            )
            val finalItem = awaitItem()
            assertThat(finalItem.roomMemberIdentityStateChanges).hasSize(1)
            val value = finalItem.roomMemberIdentityStateChanges.first()
            assertThat(value.identityRoomMember.userId).isEqualTo(A_USER_ID_2)
            assertThat(value.identityState).isEqualTo(IdentityState.PinViolation)
        }
    }

    @Test
    fun `present - when the clear room emits identity change, the presenter does not emit new state`() = runTest {
        val identityStateChanges = MutableStateFlow(emptyList<IdentityStateChange>())
        val room = FakeJoinedRoom(
            identityStateChangesFlow = identityStateChanges,
            enableEncryptionResult = { Result.success(Unit) }
        )
        val presenter = createIdentityChangeStatePresenter(room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomMemberIdentityStateChanges).isEmpty()
            identityStateChanges.emit(
                listOf(
                    IdentityStateChange(
                        userId = A_USER_ID_2,
                        identityState = IdentityState.PinViolation,
                    ),
                )
            )
            // No item emitted.
            expectNoEvents()
            // Room becomes encrypted.
            room.givenRoomInfo(aRoomInfo(isEncrypted = true))

            val finalItem = awaitItem()
            assertThat(finalItem.roomMemberIdentityStateChanges).hasSize(1)
            val value = finalItem.roomMemberIdentityStateChanges.first()
            assertThat(value.identityRoomMember.userId).isEqualTo(A_USER_ID_2)
            assertThat(value.identityRoomMember.displayNameOrDefault).isEqualTo(A_USER_ID_2.extractedDisplayName)
            assertThat(value.identityState).isEqualTo(IdentityState.PinViolation)
        }
    }

    @Test
    fun `present - when the room emits identity change, the presenter emits new state with member details`() =
        runTest {
            val identityStateChanges = MutableStateFlow(emptyList<IdentityStateChange>())
            val room = FakeJoinedRoom(identityStateChangesFlow = identityStateChanges).apply {
                givenRoomMembersState(
                    RoomMembersState.Ready(
                        listOf(
                            aRoomMember(
                                A_USER_ID_2,
                                displayName = "Alice",
                            ),
                        ).toImmutableList()
                    )
                )
                givenRoomInfo(aRoomInfo(isEncrypted = true))
            }
            val presenter = createIdentityChangeStatePresenter(room)
            presenter.test {
                val initialState = awaitItem()
                assertThat(initialState.roomMemberIdentityStateChanges).isEmpty()
                identityStateChanges.emit(
                    listOf(
                        IdentityStateChange(
                            userId = A_USER_ID_2,
                            identityState = IdentityState.PinViolation,
                        ),
                    )
                )
                val finalItem = awaitItem()
                assertThat(finalItem.roomMemberIdentityStateChanges).hasSize(1)
                val value = finalItem.roomMemberIdentityStateChanges.first()
                assertThat(value.identityRoomMember.userId).isEqualTo(A_USER_ID_2)
                assertThat(value.identityRoomMember.displayNameOrDefault).isEqualTo("Alice")
                assertThat(value.identityRoomMember.avatarData.size).isEqualTo(AvatarSize.ComposerAlert)
                assertThat(value.identityState).isEqualTo(IdentityState.PinViolation)
            }
        }

    @Test
    fun `present - when the user pins the identity, the presenter invokes the encryption service api`() =
        runTest {
            val lambda = lambdaRecorder<UserId, Result<Unit>> { Result.success(Unit) }
            val encryptionService = FakeEncryptionService(
                pinUserIdentityResult = lambda,
            )
            val presenter = createIdentityChangeStatePresenter(encryptionService = encryptionService)
            presenter.test {
                val initialState = awaitItem()
                initialState.eventSink(IdentityChangeEvent.PinIdentity(A_USER_ID))
                lambda.assertions().isCalledOnce().with(value(A_USER_ID))
            }
        }

    @Test
    fun `present - when the user withdraws the identity, the presenter invokes the encryption service api`() =
        runTest {
            val lambda = lambdaRecorder<UserId, Result<Unit>> { Result.success(Unit) }
            val encryptionService = FakeEncryptionService(
                withdrawVerificationResult = lambda,
            )
            val presenter = createIdentityChangeStatePresenter(encryptionService = encryptionService)
            presenter.test {
                val initialState = awaitItem()
                initialState.eventSink(IdentityChangeEvent.WithdrawVerification(A_USER_ID))
                lambda.assertions().isCalledOnce().with(value(A_USER_ID))
            }
        }

    private fun createIdentityChangeStatePresenter(
        room: JoinedRoom = FakeJoinedRoom(),
        encryptionService: EncryptionService = FakeEncryptionService(),
    ): IdentityChangeStatePresenter {
        return IdentityChangeStatePresenter(
            room = room,
            encryptionService = encryptionService,
        )
    }
}
