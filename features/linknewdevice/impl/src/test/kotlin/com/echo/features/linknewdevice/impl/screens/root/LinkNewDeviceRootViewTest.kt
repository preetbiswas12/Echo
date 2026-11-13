/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.linknewdevice.impl.screens.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.features.linknewdevice.impl.R
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.EnsureNeverCalledWithParam
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.ensureCalledOnceWithParam
import com.echo.tests.testutils.pressBackKey
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class LinkNewDeviceRootViewTest : RobolectricTest() {
    @Test
    fun `on back pressed - calls the onRetry callback`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<LinkNewDeviceRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLinkNewDeviceRootView(
                state = aLinkNewDeviceRootState(
                    eventSink = eventRecorder,
                ),
                onBackClick = callback
            )
            pressBackKey()
        }
    }

    @Test
    fun `link desktop button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<LinkNewDeviceRootEvent>(expectEvents = false)
        ensureCalledOnceWithParam(LinkDeviceType.Desktop) { callback ->
            setLinkNewDeviceRootView(
                state = aLinkNewDeviceRootState(
                    isSupported = AsyncData.Success(true),
                    eventSink = eventRecorder,
                ),
                onUnlockDevice = callback,
            )
            clickOn(R.string.screen_link_new_device_root_desktop_computer)
        }
    }

    @Test
    fun `link mobile button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<LinkNewDeviceRootEvent>(expectEvents = false)
        ensureCalledOnceWithParam(LinkDeviceType.Mobile) { callback ->
            setLinkNewDeviceRootView(
                state = aLinkNewDeviceRootState(
                    isSupported = AsyncData.Success(true),
                    eventSink = eventRecorder,
                ),
                onUnlockDevice = callback,
            )
            clickOn(R.string.screen_link_new_device_root_mobile_device)
        }
    }

    @Test
    fun `not supported - dismiss click - invokes the expected callback`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<LinkNewDeviceRootEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLinkNewDeviceRootView(
                state = aLinkNewDeviceRootState(
                    isSupported = AsyncData.Success(false),
                    eventSink = eventRecorder,
                ),
                onBackClick = callback,
            )
            clickOn(CommonStrings.action_dismiss)
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setLinkNewDeviceRootView(
        state: LinkNewDeviceRootState = aLinkNewDeviceRootState(),
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onUnlockDevice: (type: LinkDeviceType) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            LinkNewDeviceRootView(
                state = state,
                onBackClick = onBackClick,
                onUnlockDevice = onUnlockDevice,
            )
        }
    }
}
