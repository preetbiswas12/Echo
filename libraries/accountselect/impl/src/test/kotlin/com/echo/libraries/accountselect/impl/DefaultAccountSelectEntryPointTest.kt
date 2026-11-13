/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.accountselect.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.echo.libraries.accountselect.api.AccountSelectEntryPoint
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class DefaultAccountSelectEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultAccountSelectEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            AccountSelectNode(
                buildContext = buildContext,
                plugins = plugins,
                presenter = createAccountSelectPresenter(),
            )
        }
        val callback = object : AccountSelectEntryPoint.Callback {
            override fun onAccountSelected(sessionId: SessionId) = lambdaError()
            override fun onCancel() = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            callback = callback,
        )
        assertThat(result).isInstanceOf(AccountSelectNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}
