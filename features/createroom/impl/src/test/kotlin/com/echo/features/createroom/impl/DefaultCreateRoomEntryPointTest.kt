/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.createroom.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import com.echo.features.createroom.api.CreateRoomEntryPoint
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultCreateRoomEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultCreateRoomEntryPoint()

        val parentNode = TestParentNode.create { buildContext, plugins ->
            CreateRoomFlowNode(
                buildContext = buildContext,
                plugins = plugins,
            )
        }
        val buildContext = BuildContext.root(null)

        val callback = object : CreateRoomEntryPoint.Callback {
            override fun onRoomCreated(roomId: RoomId) = lambdaError()
        }
        val result = entryPoint
            .builder(parentNode, buildContext, callback)
            .setIsSpace(true)
            .setParentSpace(A_ROOM_ID)
            .build()
        assertThat(result.plugins).contains(callback)
    }
}

