/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import com.echo.features.home.impl.spaces.HomeSpacesPresenter
import com.echo.features.home.impl.spaces.HomeSpacesState
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.SessionScope

@BindingContainer
@ContributesTo(SessionScope::class)
interface HomeSpacesBindingContainer {
    @Binds
    fun bindHomeSpacesPresenter(presenter: HomeSpacesPresenter): Presenter<HomeSpacesState>
}

