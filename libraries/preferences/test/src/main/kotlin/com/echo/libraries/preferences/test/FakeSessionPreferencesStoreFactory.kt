/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.preferences.test

import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.preferences.api.store.SessionPreferencesStore
import com.echo.libraries.preferences.api.store.SessionPreferencesStoreFactory
import com.echo.tests.testutils.lambda.LambdaOneParamRecorder
import com.echo.tests.testutils.lambda.LambdaTwoParamsRecorder
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.CoroutineScope

class FakeSessionPreferencesStoreFactory(
    val getLambda: LambdaTwoParamsRecorder<SessionId, CoroutineScope, SessionPreferencesStore> = lambdaRecorder { _, _ -> lambdaError() },
    val removeLambda: LambdaOneParamRecorder<SessionId, Unit> = lambdaRecorder { _ -> lambdaError() },
) : SessionPreferencesStoreFactory {
    override fun get(sessionId: SessionId, sessionCoroutineScope: CoroutineScope): SessionPreferencesStore {
        return getLambda(sessionId, sessionCoroutineScope)
    }

    override fun remove(sessionId: SessionId) {
        removeLambda(sessionId)
    }
}

