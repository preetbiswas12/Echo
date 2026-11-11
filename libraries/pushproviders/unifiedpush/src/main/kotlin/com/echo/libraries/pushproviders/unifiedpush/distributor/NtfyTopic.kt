/*
 * Copyright (c) 2026 Echo contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush.distributor

import java.security.SecureRandom

/**
 * Generator of ntfy topics used as UnifiedPush endpoints.
 *
 * ntfy reserves topics starting with `up` for UnifiedPush messages sent through its Matrix
 * push gateway. The topic doubles as the only secret protecting the subscription, so it is
 * generated with enough entropy to not be guessable.
 */
internal object NtfyTopic {
    private const val PREFIX = "up"
    private const val RANDOM_PART_LENGTH = 12
    // Lowercase and uppercase letters and digits, without lookalike characters (no `l`, `I`, `O`, `0`, `1`).
    private const val ALLOWED_CHARACTERS = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /**
     * Generate a new random topic, of length [PREFIX] + [RANDOM_PART_LENGTH] = 14 characters,
     * matching the topic length used by the ntfy Android application.
     */
    fun generate(): String {
        val random = SecureRandom()
        return buildString {
            append(PREFIX)
            repeat(RANDOM_PART_LENGTH) {
                append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
            }
        }
    }
}
