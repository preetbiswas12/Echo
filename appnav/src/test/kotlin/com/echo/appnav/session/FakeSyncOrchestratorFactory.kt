/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.appnav.session

import com.echo.libraries.matrix.api.MatrixClient
import com.echo.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.CoroutineScope

class FakeSyncOrchestratorFactory : SyncOrchestrator.Factory {
    override fun create(
        matrixClient: MatrixClient,
        sessionCoroutineScope: CoroutineScope,
    ): SyncOrchestrator = lambdaError()
}

