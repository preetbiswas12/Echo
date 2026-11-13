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
import com.echo.libraries.emoji.api.recentemojis.GetRecentEmojis
import com.echo.libraries.emoji.impl.EmojibaseProvider
import com.echo.libraries.matrix.api.MatrixClient
import dev.zacsweers.metro.ContributesBinding
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext

@ContributesBinding(SessionScope::class)
class DefaultGetRecentEmojis(
    private val client: MatrixClient,
    private val dispatchers: CoroutineDispatchers,
    private val emojibaseProvider: EmojibaseProvider,
) : GetRecentEmojis {
    override suspend operator fun invoke(): Result<ImmutableList<String>> = withContext(dispatchers.io) {
        val allEmojis = emojibaseProvider.getStore().allEmojis
        client.getRecentEmojis()
            .map { emojis ->
                // Remove any possible duplicates
                emojis.distinct()
                    // Return only those emojis that are valid
                    .filter { recent -> allEmojis.any { recent == it.unicode } }
                    .toImmutableList()
            }
    }
}
