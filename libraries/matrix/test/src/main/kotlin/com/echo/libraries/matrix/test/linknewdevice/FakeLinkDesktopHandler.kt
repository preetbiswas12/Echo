/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test.linknewdevice

import com.echo.libraries.matrix.api.linknewdevice.LinkDesktopHandler
import com.echo.libraries.matrix.api.linknewdevice.LinkDesktopStep
import com.echo.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLinkDesktopHandler(
    private val handleScannedQrCodeResult: (ByteArray) -> Unit = { lambdaError() },
) : LinkDesktopHandler {
    private val mutableLinkDesktopStep: MutableStateFlow<LinkDesktopStep> = MutableStateFlow(LinkDesktopStep.Uninitialized)
    override val linkDesktopStep: StateFlow<LinkDesktopStep>
        get() = mutableLinkDesktopStep.asStateFlow()

    override suspend fun handleScannedQrCode(data: ByteArray) {
        handleScannedQrCodeResult(data)
    }

    suspend fun emitStep(step: LinkDesktopStep) {
        mutableLinkDesktopStep.emit(step)
    }
}

