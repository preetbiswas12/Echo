/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.login.impl.screens.qrcode.intro

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.features.login.impl.R
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.pressBack
import com.echo.tests.testutils.pressBackKey
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class QrCodeIntroViewTest : RobolectricTest() {
    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeIntroView(
                state = aQrCodeIntroState(),
                onBackClicked = callback
            )
            pressBackKey()
        }
    }

    @Test
    fun `on back button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeIntroView(
                state = aQrCodeIntroState(),
                onBackClicked = callback
            )
            pressBack()
        }
    }

    @Test
    fun `when can continue - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeIntroView(
                state = aQrCodeIntroState(canContinue = true),
                onContinue = callback
            )
        }
    }

    @Test
    fun `on submit button clicked - emits the Continue event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<QrCodeIntroEvent>()
        setQrCodeIntroView(
            state = aQrCodeIntroState(eventSink = eventRecorder),
        )
        clickOn(R.string.screen_qr_code_login_initial_state_button_title)
        eventRecorder.assertSingle(QrCodeIntroEvent.Continue)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setQrCodeIntroView(
        state: QrCodeIntroState,
        onBackClicked: () -> Unit = EnsureNeverCalled(),
        onContinue: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            QrCodeIntroView(
                state = state,
                onBackClick = onBackClicked,
                onContinue = onContinue,
            )
        }
    }
}
