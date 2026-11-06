/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.test

import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.push.api.GetCurrentPushProvider

class FakeGetCurrentPushProvider(
    private val currentPushProvider: String?
) : GetCurrentPushProvider {
    override suspend fun getCurrentPushProvider(sessionId: SessionId): String? = currentPushProvider
}

