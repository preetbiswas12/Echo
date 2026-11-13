/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.roomcall.impl.di

import com.echo.features.roomcall.api.RoomCallState
import com.echo.features.roomcall.impl.RoomCallStatePresenter
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.RoomScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@ContributesTo(RoomScope::class)
@BindingContainer
interface RoomCallBindingContainer {
    @Binds
    fun bindRoomCallStatePresenter(presenter: RoomCallStatePresenter): Presenter<RoomCallState>
}
