/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.compound.colors

import com.echo.compound.tokens.generated.SemanticColors
import com.echo.compound.tokens.generated.compoundColorsDark
import com.echo.compound.tokens.generated.compoundColorsLight

data class SemanticColorsLightDark(
    val light: SemanticColors,
    val dark: SemanticColors,
) {
    companion object {
        val default = SemanticColorsLightDark(
            light = compoundColorsLight,
            dark = compoundColorsDark,
        )
    }
}

