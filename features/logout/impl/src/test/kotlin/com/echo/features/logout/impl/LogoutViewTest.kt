/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.logout.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.matrix.ui.components.aMatrixUser
import com.echo.libraries.testtags.TestTags
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.pressBack
import com.echo.tests.testutils.pressTag
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class LogoutViewTest : RobolectricTest() {
    @Test
    @Config(qualifiers = "h1024dp")
    fun `the current user is displayed`() = runAndroidComposeUiTest {
        setLogoutView(
            aLogoutState(
                currentUser = aMatrixUser(id = "@alice:server.org", displayName = "Alice"),
                eventSink = EventsRecorder(expectEvents = false),
            ),
        )
        onNodeWithText("Alice").assertIsDisplayed()
        onNodeWithText("@alice:server.org").assertIsDisplayed()
    }

    @Test
    fun `clicking on logout sends a LogoutEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(
            aLogoutState(
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_signout)
        eventsRecorder.assertSingle(LogoutEvent.Logout(false))
    }

    @Test
    fun `confirming logout sends a LogoutEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(
            aLogoutState(
                logoutAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(LogoutEvent.Logout(false))
    }

    @Test
    fun `clicking on back invoke back callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on confirm after error sends a LogoutEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(
            aLogoutState(
                logoutAction = AsyncAction.Failure(Exception("Failed to logout")),
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_signout_anyway)
        eventsRecorder.assertSingle(LogoutEvent.Logout(true))
    }

    @Test
    fun `clicking on cancel after error sends a LogoutEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>()
        setLogoutView(
            aLogoutState(
                logoutAction = AsyncAction.Failure(Exception("Failed to logout")),
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_cancel, inDialog = true)
        eventsRecorder.assertSingle(LogoutEvent.CloseDialogs)
    }

    @Test
    fun `clicking on cancel invoke back callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            clickOn(CommonStrings.action_cancel)
        }
    }

    @Test
    fun `last session setting button invoke onChangeRecoveryKeyClicked`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(
                    isLastDevice = true,
                    eventSink = eventsRecorder
                ),
                onChangeRecoveryKeyClick = callback,
            )
            clickOn(CommonStrings.common_settings)
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setLogoutView(
    state: LogoutState,
    onChangeRecoveryKeyClick: () -> Unit = EnsureNeverCalled(),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        LogoutView(
            state = state,
            onChangeRecoveryKeyClick = onChangeRecoveryKeyClick,
            onBackClick = onBackClick,
        )
    }
}
