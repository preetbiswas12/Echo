/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.linknewdevice

import com.echo.libraries.matrix.api.linknewdevice.ContinuationMessageSender
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask

class FakeContinuationMessageSender(
    private val cancelResult: () -> Result<Unit> = { lambdaError() },
    private val confirmResult: () -> Result<Unit> = { lambdaError() },
) : ContinuationMessageSender {
    override suspend fun cancel(): Result<Unit> = simulateLongTask {
        cancelResult()
    }

    override suspend fun confirm(): Result<Unit> = simulateLongTask {
        confirmResult()
    }
}

