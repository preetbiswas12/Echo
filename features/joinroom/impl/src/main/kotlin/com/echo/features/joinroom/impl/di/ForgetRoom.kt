/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.joinroom.impl.di

import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.RoomId

interface ForgetRoom {
    suspend operator fun invoke(roomId: RoomId): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultForgetRoom(private val client: MatrixClient) : ForgetRoom {
    override suspend fun invoke(roomId: RoomId): Result<Unit> {
        return client.getRoom(roomId)?.use { it.forget() }
            ?: Result.failure(IllegalStateException("Room not found"))
    }
}

