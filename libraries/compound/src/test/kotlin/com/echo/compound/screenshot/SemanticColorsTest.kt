/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.compound.screenshot

import com.echo.compound.previews.CompoundSemanticColorsDark
import com.echo.compound.previews.CompoundSemanticColorsDarkHc
import com.echo.compound.previews.CompoundSemanticColorsLight
import com.echo.compound.previews.CompoundSemanticColorsLightHc
import com.echo.compound.screenshot.utils.createPaparazziRule
import org.junit.Rule
import org.junit.Test

class SemanticColorsTest {
    @get:Rule
    val paparazzi = createPaparazziRule()

    @Test
    fun screenshots() {
        paparazzi.snapshot(name = "Compound Semantic Colors - Light") {
            CompoundSemanticColorsLight()
        }

        paparazzi.snapshot(name = "Compound Semantic Colors - Light HC") {
            CompoundSemanticColorsLightHc()
        }

        paparazzi.snapshot(name = "Compound Semantic Colors - Dark") {
            CompoundSemanticColorsDark()
        }

        paparazzi.snapshot(name = "Compound Semantic Colors - Dark HC") {
            CompoundSemanticColorsDarkHc()
        }
    }
}

