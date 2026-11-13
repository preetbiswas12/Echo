/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl

import com.echo.features.preferences.api.CacheService
import com.echo.libraries.matrix.api.core.SessionId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultCacheService : CacheService {
    private val _clearedCacheEventFlow = MutableSharedFlow<SessionId>(0)
    override val clearedCacheEventFlow: Flow<SessionId> = _clearedCacheEventFlow

    suspend fun onClearedCache(sessionId: SessionId) {
        _clearedCacheEventFlow.emit(sessionId)
    }
}
