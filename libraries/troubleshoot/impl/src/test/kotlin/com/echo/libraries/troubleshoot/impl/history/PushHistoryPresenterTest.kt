/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.echo.libraries.troubleshoot.impl.history

import com.google.common.truth.Truth.assertThat
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.test.AN_EVENT_ID
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.matrix.test.A_SESSION_ID_2
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.push.api.PushService
import com.echo.libraries.push.test.FakePushService
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import com.echo.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PushHistoryPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPushHistoryPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.pushCounter).isEqualTo(0)
            assertThat(initialState.pushHistoryItems).isEmpty()
            assertThat(initialState.showOnlyErrors).isFalse()
            assertThat(initialState.resetAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.showNotSameAccountError).isFalse()
        }
    }

    @Test
    fun `present - updating state`() = runTest {
        val pushService = FakePushService()
        val presenter = createPushHistoryPresenter(
            pushService = pushService,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.pushCounter).isEqualTo(0)
            assertThat(initialState.pushHistoryItems).isEmpty()
            pushService.emitPushCounter(1)
            assertThat(awaitItem().pushCounter).isEqualTo(1)
            val item = aPushHistoryItem()
            pushService.emitPushHistoryItems(listOf(item))
            assertThat(awaitItem().pushHistoryItems).containsExactly(item)
        }
    }

    @Test
    fun `present - reset and cancel`() = runTest {
        val resetPushHistoryResult = lambdaRecorder<Unit> { }
        val pushService = FakePushService(
            resetPushHistoryResult = resetPushHistoryResult,
        )
        val presenter = createPushHistoryPresenter(
            pushService = pushService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(PushHistoryEvent.Reset(requiresConfirmation = true))
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            initialState.eventSink(PushHistoryEvent.ClearDialog)
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.Uninitialized)
            resetPushHistoryResult.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - reset and confirm`() = runTest {
        val resetPushHistoryResult = lambdaRecorder<Unit> { }
        val pushService = FakePushService(
            resetPushHistoryResult = resetPushHistoryResult,
        )
        val presenter = createPushHistoryPresenter(
            pushService = pushService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(PushHistoryEvent.Reset(requiresConfirmation = true))
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            initialState.eventSink(PushHistoryEvent.Reset(requiresConfirmation = false))
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.Loading)
            assertThat(awaitItem().resetAction).isEqualTo(AsyncAction.Uninitialized)
            resetPushHistoryResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - set show only errors`() = runTest {
        val pushService = FakePushService()
        val presenter = createPushHistoryPresenter(
            pushService = pushService,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.showOnlyErrors).isFalse()
            val item = aPushHistoryItem(hasBeenResolved = true)
            val itemError = aPushHistoryItem(hasBeenResolved = false)
            pushService.emitPushHistoryItems(listOf(item, itemError))
            awaitItem().let { state ->
                assertThat(state.pushHistoryItems).containsExactly(item, itemError)
                state.eventSink(PushHistoryEvent.SetShowOnlyErrors(showOnlyErrors = true))
            }
            skipItems(1)
            awaitItem().let { state ->
                assertThat(state.showOnlyErrors).isTrue()
                assertThat(state.pushHistoryItems).containsExactly(itemError)
                state.eventSink(PushHistoryEvent.SetShowOnlyErrors(showOnlyErrors = false))
            }
            skipItems(1)
            awaitItem().let { state ->
                assertThat(state.showOnlyErrors).isFalse()
                assertThat(state.pushHistoryItems).containsExactly(item, itemError)
            }
        }
    }

    @Test
    fun `present - item click current account`() = runTest {
        val pushHistoryNavigatorResult = lambdaRecorder<RoomId, EventId, Unit> { _, _ -> }
        val presenter = createPushHistoryPresenter(
            pushHistoryNavigator = { roomId, eventId ->
                pushHistoryNavigatorResult(roomId, eventId)
            }
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(
                PushHistoryEvent.NavigateTo(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    eventId = AN_EVENT_ID,
                )
            )
            pushHistoryNavigatorResult.assertions()
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - item click not current account`() = runTest {
        val presenter = createPushHistoryPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(
                PushHistoryEvent.NavigateTo(
                    sessionId = A_SESSION_ID_2,
                    roomId = A_ROOM_ID,
                    eventId = AN_EVENT_ID,
                )
            )
            assertThat(awaitItem().showNotSameAccountError).isTrue()
            // Reset error
            initialState.eventSink(PushHistoryEvent.ClearDialog)
            assertThat(awaitItem().showNotSameAccountError).isFalse()
        }
    }

    private fun createPushHistoryPresenter(
        pushHistoryNavigator: PushHistoryNavigator = PushHistoryNavigator { _, _ -> lambdaError() },
        pushService: PushService = FakePushService(),
        matrixClient: MatrixClient = FakeMatrixClient(),
    ): PushHistoryPresenter {
        return PushHistoryPresenter(
            pushHistoryNavigator = pushHistoryNavigator,
            pushService = pushService,
            matrixClient = matrixClient,
        )
    }
}

