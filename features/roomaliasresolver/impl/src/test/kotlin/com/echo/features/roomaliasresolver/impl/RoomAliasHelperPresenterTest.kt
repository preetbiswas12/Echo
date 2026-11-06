/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.roomaliasresolver.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.RoomAlias
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.room.alias.ResolvedRoomAlias
import com.echo.libraries.matrix.test.AN_EXCEPTION
import com.echo.libraries.matrix.test.A_ROOM_ALIAS
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.libraries.matrix.test.A_SERVER_LIST
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.tests.testutils.WarmUpRule
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.Optional
import java.util.concurrent.TimeoutException

class RoomAliasHelperPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().resolveState.isUninitialized()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - resolve alias to roomId`() = runTest {
        val result = Optional.of(aResolvedRoomAlias())
        val client = FakeMatrixClient(
            resolveRoomAliasResult = { Result.success(result) }
        )
        val presenter = createPresenter(matrixClient = client)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().resolveState.isUninitialized()).isTrue()
            assertThat(awaitItem().resolveState.isLoading()).isTrue()
            val resultState = awaitItem()
            assertThat(resultState.roomAlias).isEqualTo(A_ROOM_ALIAS)
            assertThat(resultState.resolveState.dataOrNull()).isEqualTo(result.get())
        }
    }

    @Test
    fun `present - a server that never answers ends up in an error state`() = runTest {
        val client = FakeMatrixClient(
            resolveRoomAliasResult = { awaitCancellation() }
        )
        val presenter = createPresenter(matrixClient = client)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().resolveState.isUninitialized()).isTrue()
            assertThat(awaitItem().resolveState.isLoading()).isTrue()
            val resultState = awaitItem()
            assertThat(resultState.resolveState.errorOrNull()).isInstanceOf(TimeoutException::class.java)
        }
    }

    @Test
    fun `present - resolve alias error and retry`() = runTest {
        val client = FakeMatrixClient(
            resolveRoomAliasResult = { Result.failure(AN_EXCEPTION) }
        )
        val presenter = createPresenter(matrixClient = client)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().resolveState.isUninitialized()).isTrue()
            assertThat(awaitItem().resolveState.isLoading()).isTrue()
            val resultState = awaitItem()
            assertThat(resultState.resolveState.errorOrNull()).isEqualTo(AN_EXCEPTION)
            resultState.eventSink(RoomAliasResolverEvent.Retry)
            val retryLoadingState = awaitItem()
            assertThat(retryLoadingState.resolveState.isLoading()).isTrue()
            val retryState = awaitItem()
            assertThat(retryState.resolveState.errorOrNull()).isEqualTo(AN_EXCEPTION)
        }
    }
}

internal fun createPresenter(
    roomAlias: RoomAlias = A_ROOM_ALIAS,
    matrixClient: MatrixClient = FakeMatrixClient(),
) = RoomAliasResolverPresenter(
    roomAlias = roomAlias,
    matrixClient = matrixClient,
)

internal fun aResolvedRoomAlias(
    roomId: RoomId = A_ROOM_ID,
    servers: List<String> = A_SERVER_LIST,
) = ResolvedRoomAlias(
    roomId = roomId,
    servers = servers,
)

