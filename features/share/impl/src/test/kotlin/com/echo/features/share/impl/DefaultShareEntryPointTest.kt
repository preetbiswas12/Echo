/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.share.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.echo.features.share.api.ShareEntryPoint
import com.echo.features.share.api.ShareIntentData
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.test.A_MESSAGE
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.roomselect.test.FakeRoomSelectEntryPoint
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultShareEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultShareEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            ShareNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { createSharePresenter() },
                roomSelectEntryPoint = FakeRoomSelectEntryPoint(),
            )
        }
        val callback = object : ShareEntryPoint.Callback {
            override fun onDone(sessionId: SessionId, roomIds: List<RoomId>) = lambdaError()
            override fun onCancel() = lambdaError()
            override fun onSessionVerificationRequired(sessionId: SessionId) = lambdaError()
        }
        val params = ShareEntryPoint.Params(
            sessionId = A_SESSION_ID,
            shareIntentData = ShareIntentData.PlainText(A_MESSAGE),
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(ShareNode::class.java)
        assertThat(result.plugins).contains(ShareNode.Inputs(params.sessionId, params.shareIntentData))
        assertThat(result.plugins).contains(callback)
    }
}
