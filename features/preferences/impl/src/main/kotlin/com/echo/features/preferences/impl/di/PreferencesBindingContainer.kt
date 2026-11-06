/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import com.echo.features.preferences.impl.account.PreferencesAccountPresenter
import com.echo.features.preferences.impl.account.PreferencesAccountState
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@BindingContainer
interface PreferencesBindingContainer {
    @Binds
    fun bindPreferencesAccountPresenter(presenter: PreferencesAccountPresenter): Presenter<PreferencesAccountState>
}

