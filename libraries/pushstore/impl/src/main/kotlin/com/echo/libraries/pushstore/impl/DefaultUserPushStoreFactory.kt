/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushstore.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import com.echo.libraries.di.annotations.ApplicationContext
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.preferences.api.store.PreferenceDataStoreFactory
import com.echo.libraries.pushstore.api.UserPushStore
import com.echo.libraries.pushstore.api.UserPushStoreFactory
import java.util.concurrent.ConcurrentHashMap

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultUserPushStoreFactory(
    @ApplicationContext private val context: Context,
    private val preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : UserPushStoreFactory {
    // We can have only one class accessing a single data store, so keep a cache of them.
    private val cache = ConcurrentHashMap<SessionId, UserPushStore>()
    override fun getOrCreate(userId: SessionId): UserPushStore {
        return cache.getOrPut(userId) {
            UserPushStoreDataStore(
                context = context,
                userId = userId,
                factory = preferenceDataStoreFactory,
            )
        }
    }
}

