/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.di

import com.echo.libraries.di.RoomScope
import com.echo.libraries.di.annotations.RoomCoroutineScope
import com.echo.libraries.matrix.api.room.BaseRoom
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(RoomScope::class)
object RoomBindingContainer {
    @RoomCoroutineScope
    @Provides
    fun providesSessionCoroutineScope(room: BaseRoom): CoroutineScope {
        return room.roomCoroutineScope
    }
}
