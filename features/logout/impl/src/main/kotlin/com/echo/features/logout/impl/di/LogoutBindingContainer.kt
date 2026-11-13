/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.logout.impl.di

import com.echo.features.logout.api.direct.DirectLogoutState
import com.echo.features.logout.impl.direct.DirectLogoutPresenter
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.SessionScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@ContributesTo(SessionScope::class)
@BindingContainer
interface LogoutBindingContainer {
    @Binds
    fun bindDirectLogoutPresenter(presenter: DirectLogoutPresenter): Presenter<DirectLogoutState>
}
