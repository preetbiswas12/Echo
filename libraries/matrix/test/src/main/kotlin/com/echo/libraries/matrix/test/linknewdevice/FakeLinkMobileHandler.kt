/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test.linknewdevice

import com.echo.libraries.matrix.api.linknewdevice.LinkMobileHandler
import com.echo.libraries.matrix.api.linknewdevice.LinkMobileStep
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLinkMobileHandler(
    private val startResult: () -> Unit = { lambdaError() },
) : LinkMobileHandler {
    private val mutableLinkMobileStep: MutableStateFlow<LinkMobileStep> = MutableStateFlow(LinkMobileStep.Uninitialized)
    override val linkMobileStep: StateFlow<LinkMobileStep>
        get() = mutableLinkMobileStep.asStateFlow()

    override suspend fun start() = simulateLongTask {
        startResult()
    }

    suspend fun emitStep(step: LinkMobileStep) {
        mutableLinkMobileStep.emit(step)
    }
}

