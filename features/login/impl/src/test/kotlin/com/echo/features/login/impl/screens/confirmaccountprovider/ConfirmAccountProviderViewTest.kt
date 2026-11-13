/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.login.impl.screens.confirmaccountprovider

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.libraries.matrix.api.auth.OAuthDetails
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.EnsureNeverCalledWithParam
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.pressBack
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ConfirmAccountProviderViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<ConfirmAccountProviderEvent>(expectEvents = false)
        ensureCalledOnce {
            setConfirmAccountProviderView(
                state = aConfirmAccountProviderState(
                    eventSink = eventSink,
                ),
                onBackClick = it,
            )
            pressBack()
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setConfirmAccountProviderView(
        state: ConfirmAccountProviderState,
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onOAuthDetails: (OAuthDetails) -> Unit = EnsureNeverCalledWithParam(),
        onNeedLoginPassword: () -> Unit = EnsureNeverCalled(),
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            ConfirmAccountProviderView(
                state = state,
                onBackClick = onBackClick,
                onOAuthDetails = onOAuthDetails,
                onNeedLoginPassword = onNeedLoginPassword,
                onLearnMoreClick = onLearnMoreClick,
            )
        }
    }
}
