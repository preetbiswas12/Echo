/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.tasks

import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.push.api.notifications.NotificationCleaner
import kotlinx.coroutines.withContext

interface MarkAllRoomsAsRead {
    suspend operator fun invoke(): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultMarkAllRoomsAsRead(
    private val client: MatrixClient,
    private val notificationCleaner: NotificationCleaner,
    private val coroutineDispatchers: CoroutineDispatchers,
) : MarkAllRoomsAsRead {
    override suspend fun invoke(): Result<Unit> = withContext(coroutineDispatchers.io) {
        client.markAllRoomsAsRead()
            .onSuccess {
                notificationCleaner.clearAllMessagesEvents(client.sessionId)
            }
    }
}

