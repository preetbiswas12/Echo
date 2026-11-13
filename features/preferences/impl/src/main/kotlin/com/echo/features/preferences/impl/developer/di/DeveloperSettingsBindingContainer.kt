/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.developer.di

import com.echo.features.preferences.impl.developer.appsettings.AppDeveloperSettingsPresenter
import com.echo.features.preferences.impl.developer.appsettings.AppDeveloperSettingsState
import com.echo.libraries.architecture.Presenter
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
@BindingContainer
interface DeveloperSettingsBindingContainer {
    @Binds
    fun bindAppDeveloperSettingsPresenter(presenter: AppDeveloperSettingsPresenter): Presenter<AppDeveloperSettingsState>
}
