/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.linknewdevice.impl.screens.qrcode

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.pressBackKey
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ShowQrCodeViewTest : RobolectricTest() {
    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                onBackClick = callback
            )
            pressBackKey()
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setView(
        onBackClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            ShowQrCodeView(
                state = aShowQrCodeState(),
                onBackClick = onBackClick,
            )
        }
    }
}

