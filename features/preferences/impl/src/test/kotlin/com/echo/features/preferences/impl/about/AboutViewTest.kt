/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.preferences.impl.about

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.tests.testutils.EnsureNeverCalled
import com.echo.tests.testutils.EnsureNeverCalledWithParam
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.ensureCalledOnce
import com.echo.tests.testutils.ensureCalledOnceWithParam
import com.echo.tests.testutils.pressBack
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class AboutViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes back callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setAboutView(
                anAboutState(),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on an item invokes the expected callback`() = runAndroidComposeUiTest {
        val state = anAboutState()
        ensureCalledOnceWithParam(state.elementLegals.first()) { callback ->
            setAboutView(
                state,
                onElementLegalClick = callback,
            )
            clickOn(state.elementLegals.first().titleRes)
        }
    }

    @Test
    fun `clicking on the open source licenses invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setAboutView(
                anAboutState(),
                onOpenSourceLicensesClick = callback,
            )
            clickOn(CommonStrings.common_open_source_licenses)
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setAboutView(
    state: AboutState,
    onElementLegalClick: (ElementLegal) -> Unit = EnsureNeverCalledWithParam(),
    onOpenSourceLicensesClick: () -> Unit = EnsureNeverCalled(),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AboutView(
            state = state,
            onElementLegalClick = onElementLegalClick,
            onOpenSourceLicensesClick = onOpenSourceLicensesClick,
            onBackClick = onBackClick,
        )
    }
}

