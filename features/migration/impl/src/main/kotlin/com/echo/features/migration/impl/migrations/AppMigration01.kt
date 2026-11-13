/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.migration.impl.migrations

import com.echo.features.rageshake.api.logs.LogFilesRemover
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

/**
 * Remove existing logs from the device to remove any leaks of sensitive data.
 */
@ContributesIntoSet(AppScope::class)
class AppMigration01(
    private val logFilesRemover: LogFilesRemover,
) : AppMigration {
    override val order: Int = 1

    override suspend fun migrate(isFreshInstall: Boolean) {
        logFilesRemover.perform()
    }
}
