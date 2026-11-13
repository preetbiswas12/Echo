/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl

import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.push.api.GetCurrentPushProvider
import com.echo.libraries.pushstore.api.UserPushStoreFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class DefaultGetCurrentPushProvider(
    private val pushStoreFactory: UserPushStoreFactory,
) : GetCurrentPushProvider {
    override suspend fun getCurrentPushProvider(sessionId: SessionId): String? {
        return pushStoreFactory.getOrCreate(sessionId).getPushProviderName()
    }
}
