/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.ftue.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.echo.features.ftue.api.SessionVerificationEntryPoint
import com.echo.features.ftue.impl.sessionverification.FtueSessionVerificationFlowNode
import com.echo.features.securebackup.api.SecureBackupEntryPoint
import com.echo.features.verifysession.api.OutgoingVerificationEntryPoint
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultSessionVerificationEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultSessionVerificationEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            FtueSessionVerificationFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                outgoingVerificationEntryPoint = object : OutgoingVerificationEntryPoint {
                    override fun createNode(
                        parentNode: Node,
                        buildContext: BuildContext,
                        params: OutgoingVerificationEntryPoint.Params,
                        callback: OutgoingVerificationEntryPoint.Callback,
                    ) = lambdaError()
                },
                secureBackupEntryPoint = object : SecureBackupEntryPoint {
                    override fun createNode(
                        parentNode: Node,
                        buildContext: BuildContext,
                        params: SecureBackupEntryPoint.Params,
                        callback: SecureBackupEntryPoint.Callback,
                    ) = lambdaError()
                },
            )
        }
        val callback = object : SessionVerificationEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val result = entryPoint.createNode(parentNode, BuildContext.root(null), callback)
        assertThat(result).isInstanceOf(FtueSessionVerificationFlowNode::class.java)
    }
}
