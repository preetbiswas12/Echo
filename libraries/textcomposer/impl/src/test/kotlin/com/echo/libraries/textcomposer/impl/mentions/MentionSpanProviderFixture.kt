/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.textcomposer.impl.mentions

import com.echo.libraries.matrix.api.permalink.PermalinkParser
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.permalink.FakePermalinkParser
import com.echo.libraries.textcomposer.mentions.MentionSpanFormatter
import com.echo.libraries.textcomposer.mentions.MentionSpanProvider
import com.echo.libraries.textcomposer.mentions.MentionSpanTheme
import com.echo.libraries.textcomposer.mentions.MentionType

fun aMentionSpanProvider(
    permalinkParser: PermalinkParser = FakePermalinkParser(),
    mentionSpanFormatter: MentionSpanFormatter = object : MentionSpanFormatter {
        override fun formatDisplayText(mentionType: MentionType): CharSequence {
            return mentionType.toString()
        }
    },
    mentionSpanTheme: MentionSpanTheme = MentionSpanTheme(A_USER_ID),
): MentionSpanProvider {
    return MentionSpanProvider(
        permalinkParser = permalinkParser,
        mentionSpanFormatter = mentionSpanFormatter,
        mentionSpanTheme = mentionSpanTheme,
    )
}
