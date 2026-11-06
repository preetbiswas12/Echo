/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test

import com.echo.libraries.matrix.api.SdkPendingTask
import com.echo.tests.testutils.lambda.lambdaError

class FakeSdkPendingTask(
    private val isRunningLambda: () -> Boolean = { lambdaError() },
    private val closeLambda: () -> Unit = { lambdaError() },
) : SdkPendingTask {
    override fun isRunning(): Boolean = isRunningLambda()

    override fun close() = closeLambda()
}

