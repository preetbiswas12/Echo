/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.reportroom.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.pressBack
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ReportRoomViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invoke the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ReportRoomEvent>(expectEvents = false)
        ensureCalledOnce {
            setReportRoomView(
                aReportRoomState(
                    eventSink = eventsRecorder,
                ),
                onBackClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on report when enabled emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ReportRoomEvent>()
        setReportRoomView(
            aReportRoomState(
                reason = "Spam",
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_report)
        eventsRecorder.assertSingle(ReportRoomEvent.Report)
    }

    @Test
    fun `clicking on decline when disabled does not emit event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ReportRoomEvent>(expectEvents = false)
        setReportRoomView(
            aReportRoomState(eventSink = eventsRecorder),
        )
        clickOn(CommonStrings.action_report)
    }

    @Test
    fun `clicking on leave room option emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ReportRoomEvent>()
        setReportRoomView(
            aReportRoomState(eventSink = eventsRecorder),
        )
        clickOn(CommonStrings.action_leave_room)
        eventsRecorder.assertSingle(ReportRoomEvent.ToggleLeaveRoom)
    }

    @Test
    fun `typing text in the reason field emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ReportRoomEvent>()
        setReportRoomView(
            aReportRoomState(
                eventSink = eventsRecorder,
                reason = ""
            ),
        )
        onNodeWithText("").performTextInput("Spam!")
        eventsRecorder.assertSingle(ReportRoomEvent.UpdateReason("Spam!"))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setReportRoomView(
    state: ReportRoomState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ReportRoomView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
