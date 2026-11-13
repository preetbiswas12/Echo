/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invite.impl

import android.content.Context
import com.echo.features.invite.api.SeenInvitesStore
import com.echo.libraries.di.annotations.ApplicationContext
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.sessionstorage.api.observer.SessionObserver
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultSeenInvitesStoreFactory(
    @ApplicationContext private val context: Context,
    private val sessionObserver: SessionObserver,
) : SeenInvitesStoreFactory {
    // We can have only one class accessing a single data store, so keep a cache of them.
    private val cache = ConcurrentHashMap<SessionId, SeenInvitesStore>()

    override fun getOrCreate(
        sessionId: SessionId,
        sessionCoroutineScope: CoroutineScope,
    ): SeenInvitesStore {
        return cache.getOrPut(sessionId) {
            DefaultSeenInvitesStore(
                context = context,
                sessionId = sessionId,
                sessionCoroutineScope = sessionCoroutineScope,
                sessionObserver = sessionObserver,
            )
        }
    }
}
