/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.linknewdevice.impl.screens.scan

import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.matrix.test.AN_EXCEPTION
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.pressBackKey
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class ScanQrCodeViewTest : RobolectricTest() {
    @Test
    @Config(sdk = [UPSIDE_DOWN_CAKE])
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<ScanQrCodeEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                state = aScanQrCodeState(
                    eventSink = eventRecorder,
                ),
                onBackClick = callback
            )
            pressBackKey()
        }
    }

    @Test
    @Config(sdk = [UPSIDE_DOWN_CAKE])
    fun `try again button clicked - emits the expected event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<ScanQrCodeEvent>()
        setView(
            state = aScanQrCodeState(
                scanAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventRecorder,
            )
        )
        clickOn(CommonStrings.action_try_again)
        eventRecorder.assertSingle(ScanQrCodeEvent.TryAgain)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setView(
        state: ScanQrCodeState = aScanQrCodeState(),
        onBackClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ScanQrCodeView(
                    state = state,
                    onBackClick = onBackClick,
                )
            }
        }
    }
}

