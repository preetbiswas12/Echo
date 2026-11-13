/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.echo.libraries.matrix.api.user.DisplayedStatus
import com.echo.libraries.ui.strings.CommonStrings

fun DisplayedStatus.toEmojiText(): String = when (this) {
    is DisplayedStatus.UserSet -> status.emoji
    is DisplayedStatus.InCall -> "🎧"
}

@Composable
fun DisplayedStatus.toText(): String = when (this) {
    is DisplayedStatus.UserSet -> "${status.emoji} ${status.text}"
    is DisplayedStatus.InCall -> "🎧 ${stringResource(CommonStrings.common_on_a_call)}"
}
