/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.userstatus.di

import com.echo.features.preferences.impl.userstatus.UserStatusPresenter
import com.echo.features.preferences.impl.userstatus.UserStatusState
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.SessionScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@ContributesTo(SessionScope::class)
@BindingContainer
interface UserStatusBindingContainer {
    @Binds
    fun bindUserStatusPresenter(presenter: UserStatusPresenter): Presenter<UserStatusState>
}
