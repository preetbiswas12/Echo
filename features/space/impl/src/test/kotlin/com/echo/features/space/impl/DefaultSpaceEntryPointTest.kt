/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.space.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.echo.features.createroom.test.FakeCreateRoomEntryPoint
import com.echo.features.rolesandpermissions.test.FakeChangeRoomMemberRolesEntryPoint
import com.echo.features.space.api.SpaceEntryPoint
import com.echo.features.space.impl.di.FakeSpaceFlowGraph
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.libraries.matrix.test.room.FakeJoinedRoom
import com.echo.libraries.matrix.test.spaces.FakeSpaceRoomList
import com.echo.libraries.matrix.test.spaces.FakeSpaceService
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultSpaceEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultSpaceEntryPoint()
        val nodeInputs = SpaceEntryPoint.Inputs(A_ROOM_ID)
        val parentNode = TestParentNode.create { buildContext, plugins ->
            SpaceFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                spaceService = FakeSpaceService(
                    spaceRoomListResult = { _: RoomId -> FakeSpaceRoomList(A_ROOM_ID) }
                ),
                room = FakeJoinedRoom(),
                graphFactory = FakeSpaceFlowGraph.Factory,
                createRoomEntryPoint = FakeCreateRoomEntryPoint(),
                changeRoomMemberRolesEntryPoint = FakeChangeRoomMemberRolesEntryPoint(),
                sessionCoroutineScope = backgroundScope,
            )
        }
        val callback = object : SpaceEntryPoint.Callback {
            override fun navigateToRoom(roomId: RoomId, viaParameters: List<String>) = lambdaError()
            override fun navigateToRoomMemberList() = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = nodeInputs,
            callback = callback,
        )
        assertThat(result).isInstanceOf(SpaceFlowNode::class.java)
        assertThat(result.plugins).contains(nodeInputs)
        assertThat(result.plugins).contains(callback)
    }
}
