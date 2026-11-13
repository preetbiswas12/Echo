/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.preferences.impl.developer.appsettings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isEditable
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.features.preferences.impl.R
import com.echo.features.preferences.impl.developer.tracing.LogLevelItem
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.pressBack
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class AppDeveloperSettingsPageTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AppDeveloperSettingsEvent>(expectEvents = false)
        ensureCalledOnce {
            setAppDeveloperSettingsView(
                state = anAppDeveloperSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on View source emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AppDeveloperSettingsEvent>()
        setAppDeveloperSettingsView(
            state = anAppDeveloperSettingsState(
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_view_source)
        eventsRecorder.assertSingle(AppDeveloperSettingsEvent.SetDeveloperModeEnabled(true))
    }

    @Config(qualifiers = "h1500dp")
    @Test
    fun `clicking on element call url open the dialogs and submit emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AppDeveloperSettingsEvent>()
        setAppDeveloperSettingsView(
            state = anAppDeveloperSettingsState(
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_advanced_settings_element_call_base_url)
        val textInputNode = onAllNodes(isEditable().and(isFocusable())).filterToOne(hasAnyAncestor(isDialog()))
        textInputNode.performTextInput("https://call.element.dev")
        clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(AppDeveloperSettingsEvent.SetCustomElementCallBaseUrl("https://call.element.dev"))
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on log level emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AppDeveloperSettingsEvent>()
        setAppDeveloperSettingsView(
            state = anAppDeveloperSettingsState(
                eventSink = eventsRecorder
            ),
        )
        onNodeWithText("Tracing log level").performClick()
        onNodeWithText("Debug").performClick()
        eventsRecorder.assertSingle(AppDeveloperSettingsEvent.SetTracingLogLevel(LogLevelItem.DEBUG))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setAppDeveloperSettingsView(
    state: AppDeveloperSettingsState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AppDeveloperSettingsPage(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
