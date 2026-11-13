/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.migration.impl.migrations

import com.echo.libraries.matrix.api.MatrixClientProvider
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.sessionstorage.api.SessionStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

/**
 * Ensure we clear the well-known cached config, since it could be invalid due to an SDK issue.
 */
@ContributesIntoSet(AppScope::class)
class AppMigration09(
    private val sessionStore: SessionStore,
    private val matrixClientProvider: MatrixClientProvider,
) : AppMigration {
    override val order: Int = 9

    override suspend fun migrate(isFreshInstall: Boolean) {
        if (isFreshInstall) return

        val sessions = sessionStore.getAllSessions()

        for (session in sessions) {
            val client = matrixClientProvider.getOrRestore(SessionId(session.userId)).getOrNull() ?: continue
            client.resetWellKnownConfig()
        }
    }
}
