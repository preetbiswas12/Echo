/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.test.timeline

import com.echo.features.messages.api.timeline.TextPillificator

class FakeTextPillificator(
    private val pillifyLambda: (CharSequence, Boolean) -> CharSequence = { text, _ -> text }
) : TextPillificator {
    override fun pillify(text: CharSequence, pillifyPermalinks: Boolean): CharSequence {
        return pillifyLambda(text, pillifyPermalinks)
    }
}
