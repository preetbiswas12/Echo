/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.workmanager.test

import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.workmanager.api.WorkManagerRequestBuilder
import com.echo.libraries.workmanager.api.WorkManagerRequestType
import com.echo.libraries.workmanager.api.WorkManagerScheduler
import com.echo.tests.testutils.lambda.lambdaError

class FakeWorkManagerScheduler(
    private val submitLambda: (WorkManagerRequestBuilder) -> Unit = { lambdaError() },
    private val hasPendingWorkLambda: (SessionId, WorkManagerRequestType) -> Boolean = { _, _ -> false },
    private val cancelLambda: (SessionId, WorkManagerRequestType?) -> Unit = { _, _ -> lambdaError() },
) : WorkManagerScheduler {
    override suspend fun submit(workManagerRequestBuilder: WorkManagerRequestBuilder) {
        submitLambda(workManagerRequestBuilder)
    }

    override fun hasPendingWork(sessionId: SessionId, requestType: WorkManagerRequestType): Boolean {
        return hasPendingWorkLambda(sessionId, requestType)
    }

    override fun cancel(sessionId: SessionId, requestType: WorkManagerRequestType?) {
        cancelLambda(sessionId, requestType)
    }
}

