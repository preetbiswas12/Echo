/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.htmlrenderer.api.spans

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import com.echo.compound.theme.ElementTheme

/**
 * The style for inline code spans.
 */
val InlineCodeSpanStyle: SpanStyle = SpanStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = ElementTheme.typography.fontBodySmRegular.fontSize,
)

