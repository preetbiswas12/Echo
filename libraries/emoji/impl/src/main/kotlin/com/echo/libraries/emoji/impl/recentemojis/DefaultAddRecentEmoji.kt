/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.emoji.impl.recentemojis

import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.di.SessionScope
import com.echo.libraries.emoji.api.recentemojis.AddRecentEmoji
import com.echo.libraries.matrix.api.MatrixClient
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext

@ContributesBinding(SessionScope::class)
class DefaultAddRecentEmoji(
    private val client: MatrixClient,
    private val dispatchers: CoroutineDispatchers,
) : AddRecentEmoji {
    override suspend operator fun invoke(emoji: String): Result<Unit> = withContext(dispatchers.io) {
        client.addRecentEmoji(emoji)
    }
}
