/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl.di

import com.echo.features.home.impl.filters.RoomListFiltersPresenter
import com.echo.features.home.impl.filters.RoomListFiltersState
import com.echo.features.home.impl.roomlist.RoomListPresenter
import com.echo.features.home.impl.roomlist.RoomListState
import com.echo.features.home.impl.search.GlobalSearchPresenter
import com.echo.features.home.impl.search.GlobalSearchState
import com.echo.features.home.impl.search.RoomListSearchPresenter
import com.echo.features.home.impl.search.RoomListSearchState
import com.echo.features.home.impl.spacefilters.SpaceFiltersPresenter
import com.echo.features.home.impl.spacefilters.SpaceFiltersState
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.SessionScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@ContributesTo(SessionScope::class)
@BindingContainer
interface RoomListBindingContainer {
    @Binds
    fun bindRoomListPresenter(presenter: RoomListPresenter): Presenter<RoomListState>

    @Binds
    fun bindRoomListSearchPresenter(presenter: RoomListSearchPresenter): Presenter<RoomListSearchState>

    @Binds
    fun bindRoomListFiltersPresenter(presenter: RoomListFiltersPresenter): Presenter<RoomListFiltersState>

    @Binds
    fun bindSpaceFiltersPresenter(presenter: SpaceFiltersPresenter): Presenter<SpaceFiltersState>

    @Binds
    fun bindGlobalSearchPresenter(presenter: GlobalSearchPresenter): Presenter<GlobalSearchState>
}
