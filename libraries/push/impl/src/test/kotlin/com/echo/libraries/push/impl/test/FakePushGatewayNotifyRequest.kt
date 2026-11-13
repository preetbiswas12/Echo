/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.test

import com.echo.libraries.push.impl.pushgateway.PushGatewayNotifyRequest
import com.echo.tests.testutils.lambda.lambdaError

class FakePushGatewayNotifyRequest(
    private val executeResult: (PushGatewayNotifyRequest.Params) -> Unit = { lambdaError() }
) : PushGatewayNotifyRequest {
    override suspend fun execute(params: PushGatewayNotifyRequest.Params) {
        executeResult(params)
    }
}
