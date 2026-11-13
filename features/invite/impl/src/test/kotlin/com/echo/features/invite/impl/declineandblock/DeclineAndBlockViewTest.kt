/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.invite.impl.declineandblock

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.features.invite.impl.R
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.pressBack
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class DeclineAndBlockViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invoke the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvent>(expectEvents = false)
        ensureCalledOnce {
            setDeclineAndBlockView(
                aDeclineAndBlockState(
                    eventSink = eventsRecorder,
                ),
                onBackClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `a successful decline invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvent>(expectEvents = false)
        ensureCalledOnce {
            setDeclineAndBlockView(
                aDeclineAndBlockState(
                    declineAction = AsyncAction.Success(Unit),
                    eventSink = eventsRecorder,
                ),
                onDeclineSuccess = it,
            )
        }
    }

    @Test
    fun `clicking on decline when enabled emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvent>()
        setDeclineAndBlockView(
            aDeclineAndBlockState(
                blockUser = true,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_decline)
        eventsRecorder.assertSingle(DeclineAndBlockEvent.Decline)
    }

    @Test
    fun `clicking on decline when disabled does not emit event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvent>(expectEvents = false)
        setDeclineAndBlockView(
            aDeclineAndBlockState(
                blockUser = false,
                reportRoom = false,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_decline)
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `clicking on block option emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvent>()
        setDeclineAndBlockView(
            aDeclineAndBlockState(
                blockUser = true,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_decline_and_block_block_user_option_title)
        eventsRecorder.assertSingle(DeclineAndBlockEvent.ToggleBlockUser)
    }

    @Test
    fun `clicking on report room option emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvent>()
        setDeclineAndBlockView(
            aDeclineAndBlockState(
                reportRoom = true,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_report_room)
        eventsRecorder.assertSingle(DeclineAndBlockEvent.ToggleReportRoom)
    }

    @Test
    fun `typing text in the reason field emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<DeclineAndBlockEvent>()
        setDeclineAndBlockView(
            aDeclineAndBlockState(
                reportRoom = true,
                reportReason = "",
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText("").performTextInput("Spam!")
        eventsRecorder.assertSingle(DeclineAndBlockEvent.UpdateReportReason("Spam!"))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setDeclineAndBlockView(
    state: DeclineAndBlockState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onDeclineSuccess: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        DeclineAndBlockView(
            state = state,
            onBackClick = onBackClick,
            onDeclineSuccess = onDeclineSuccess,
        )
    }
}
