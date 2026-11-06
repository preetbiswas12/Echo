/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.knockrequests.impl.data.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import com.echo.features.knockrequests.api.KnockRequestPermissions
import com.echo.features.knockrequests.api.knockRequestPermissions
import com.echo.features.knockrequests.impl.data.KnockRequestsService
import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.libraries.matrix.api.room.powerlevels.permissionsFlow

@BindingContainer
@ContributesTo(RoomScope::class)
object KnockRequestsBindingContainer {
    @Provides
    @SingleIn(RoomScope::class)
    fun providesKnockRequestsService(room: JoinedRoom): KnockRequestsService {
        return KnockRequestsService(
            knockRequestsFlow = room.knockRequestsFlow,
            permissionsFlow = room.permissionsFlow(KnockRequestPermissions.DEFAULT) { perms ->
                perms.knockRequestPermissions()
            },
            coroutineScope = room.roomCoroutineScope,
        )
    }
}

