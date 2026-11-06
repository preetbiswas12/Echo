/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.forward.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.test.AN_EXCEPTION
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.libraries.testtags.TestTags
import com.echo.tests.testutils.EnsureNeverCalledWithParam
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.ensureCalledOnceWithParam
import com.echo.tests.testutils.pressTag
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ForwardMessagesViewTest : RobolectricTest() {
    @Test
    fun `cancel error emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ForwardMessagesEvent>()
        setForwardMessagesView(
            aForwardMessagesState(
                forwardAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventsRecorder
            ),
        )
        pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(ForwardMessagesEvent.ClearError)
    }

    @Test
    fun `success invokes onForwardSuccess`() = runAndroidComposeUiTest {
        val data = listOf(A_ROOM_ID)
        val eventsRecorder = EventsRecorder<ForwardMessagesEvent>(expectEvents = false)
        ensureCalledOnceWithParam<List<RoomId>?>(data) { callback ->
            setForwardMessagesView(
                aForwardMessagesState(
                    forwardAction = AsyncAction.Success(data),
                    eventSink = eventsRecorder
                ),
                onForwardSuccess = callback,
            )
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setForwardMessagesView(
    state: ForwardMessagesState,
    onForwardSuccess: (List<RoomId>) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        ForwardMessagesView(
            state = state,
            onForwardSuccess = onForwardSuccess,
        )
    }
}

