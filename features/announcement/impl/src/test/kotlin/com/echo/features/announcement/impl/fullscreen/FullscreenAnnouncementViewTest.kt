/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.announcement.impl.fullscreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.features.announcement.api.Announcement
import com.echo.features.announcement.impl.AnnouncementEvent
import com.echo.features.announcement.impl.AnnouncementState
import com.echo.features.announcement.impl.anAnnouncementState
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.pressBackKey
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class FullscreenAnnouncementViewTest : RobolectricTest() {
    @Test
    fun `clicking on back sends a AnnouncementEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AnnouncementEvent>()
        setFullscreenAnnouncementView(
            anAnnouncementState(
                announcement = Announcement.Fullscreen.Space,
                eventSink = eventsRecorder,
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(AnnouncementEvent.Continue(Announcement.Fullscreen.Space))
    }

    @Test
    fun `clicking on Continue sends a AnnouncementEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AnnouncementEvent>()
        setFullscreenAnnouncementView(
            anAnnouncementState(
                announcement = Announcement.Fullscreen.Space,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(AnnouncementEvent.Continue(Announcement.Fullscreen.Space))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setFullscreenAnnouncementView(
    state: AnnouncementState,
) {
    setContent {
        FullscreenAnnouncementView(
            state = state,
        )
    }
}

