/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush.troubleshoot

import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.pushproviders.api.Config
import com.echo.libraries.pushproviders.unifiedpush.UnifiedPushSessionPushConfigProvider
import com.echo.tests.testutils.lambda.lambdaError

class FakeUnifiedPushSessionPushConfigProvider(
    private val config: (SessionId) -> Config? = { lambdaError() },
) : UnifiedPushSessionPushConfigProvider {
    override suspend fun provide(sessionId: SessionId): Config? {
        return config(sessionId)
    }
}

