/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.services.appnavstate.impl.initializer

import android.content.Context
import androidx.lifecycle.ProcessLifecycleInitializer
import androidx.startup.Initializer
import com.echo.services.appnavstate.api.AppForegroundStateService
import com.echo.services.appnavstate.impl.DefaultAppForegroundStateService

class AppForegroundStateServiceInitializer : Initializer<AppForegroundStateService> {
    override fun create(context: Context): AppForegroundStateService {
        return DefaultAppForegroundStateService()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf(
        ProcessLifecycleInitializer::class.java
    )
}
