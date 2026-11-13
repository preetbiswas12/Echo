/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.rageshake.impl.di

import com.echo.features.rageshake.impl.crash.PreferencesCrashDataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface RageshakeBindings {
    fun preferencesCrashDataStore(): PreferencesCrashDataStore
}
