/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.roomselect.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.roomselect.api.RoomSelectEntryPoint
import com.echo.libraries.roomselect.api.RoomSelectMode
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import com.echo.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultRoomSelectEntryPointTest : RobolectricTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultRoomSelectEntryPoint()
        val testMode = RoomSelectMode.Share
        val testMaxNumberOfRooms = 12
        val parentNode = TestParentNode.create { buildContext, plugins ->
            RoomSelectNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { initialSessionId, mode, maxNumberOfRooms, _ ->
                    assertThat(initialSessionId).isEqualTo(A_SESSION_ID)
                    assertThat(mode).isEqualTo(testMode)
                    assertThat(maxNumberOfRooms).isEqualTo(testMaxNumberOfRooms)
                    createRoomSelectPresenter(mode = mode, maxNumberOfRooms = maxNumberOfRooms)
                },
            )
        }
        val callback = object : RoomSelectEntryPoint.Callback {
            override fun onRoomSelected(sessionId: SessionId, roomIds: List<RoomId>) = lambdaError()
            override fun onCancel() = lambdaError()
            override fun onSessionVerificationRequired(sessionId: SessionId) = lambdaError()
        }
        val params = RoomSelectEntryPoint.Params(
            sessionId = A_SESSION_ID,
            mode = testMode,
            maxNumberOfRooms = testMaxNumberOfRooms,
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(RoomSelectNode::class.java)
        assertThat(result.plugins).contains(RoomSelectNode.Inputs(params.sessionId, params.mode, params.maxNumberOfRooms))
        assertThat(result.plugins).contains(callback)
    }
}

