/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.preferences.impl.store.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import com.echo.libraries.di.SessionScope
import com.echo.libraries.di.annotations.SessionCoroutineScope
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.preferences.api.store.SessionPreferencesStore
import com.echo.libraries.preferences.impl.store.DefaultSessionPreferencesStoreFactory
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(SessionScope::class)
object SessionPreferencesBindingContainer {
    @Provides
    fun providesSessionPreferencesStore(
        defaultSessionPreferencesStoreFactory: DefaultSessionPreferencesStoreFactory,
        sessionId: SessionId,
        @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
    ): SessionPreferencesStore {
        return defaultSessionPreferencesStoreFactory
            .get(sessionId, sessionCoroutineScope)
    }
}

