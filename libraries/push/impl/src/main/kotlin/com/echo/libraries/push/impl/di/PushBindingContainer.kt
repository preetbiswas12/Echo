/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.di

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.annotations.ApplicationContext
import com.echo.libraries.push.api.battery.BatteryOptimizationState
import com.echo.libraries.push.impl.battery.BatteryOptimizationPresenter
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
interface PushBindingContainer {
    companion object {
        @Provides
        fun providesNotificationManagerCompat(@ApplicationContext context: Context): NotificationManagerCompat {
            return NotificationManagerCompat.from(context)
        }
    }

    @Binds
    fun bindBatteryOptimizationPresenter(presenter: BatteryOptimizationPresenter): Presenter<BatteryOptimizationState>
}
