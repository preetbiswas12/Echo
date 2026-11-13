/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.linknewdevice.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.echo.features.enterprise.test.FakeSessionEnterpriseService
import com.echo.features.linknewdevice.api.LinkNewDeviceEntryPoint
import com.echo.features.lockscreen.test.FakeDeviceUnlockEntryPoint
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultLinkNewDeviceEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node creation`() = runTest {
        val entryPoint = DefaultLinkNewDeviceEntryPoint()
        val client = FakeMatrixClient()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            LinkNewDeviceFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                sessionCoroutineScope = backgroundScope,
                linkNewMobileHandler = LinkNewMobileHandler(client),
                linkNewDesktopHandler = LinkNewDesktopHandler(client),
                sessionEnterpriseService = FakeSessionEnterpriseService(),
                deviceUnlockEntryPoint = FakeDeviceUnlockEntryPoint(),
            )
        }
        val callback: LinkNewDeviceEntryPoint.Callback = object : LinkNewDeviceEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val result = entryPoint.createNode(parentNode, BuildContext.root(null), callback)
        assertThat(result).isInstanceOf(LinkNewDeviceFlowNode::class.java)
    }
}
