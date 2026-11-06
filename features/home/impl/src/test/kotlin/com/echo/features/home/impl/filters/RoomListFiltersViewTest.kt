/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package com.echo.features.home.impl.filters

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.features.home.impl.R
import com.echo.features.home.impl.filters.selection.FilterSelectionState
import com.echo.libraries.testtags.TestTags
import com.echo.tests.testutils.EventsRecorder
import com.echo.tests.testutils.clickOn
import com.echo.tests.testutils.pressTag
import com.echo.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class RoomListFiltersViewTest : RobolectricTest() {
    @Test
    fun `clicking on filters generates expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListFiltersEvent>()
        setContent {
            RoomListFiltersView(
                state = aRoomListFiltersState(eventSink = eventsRecorder),
            )
        }
        clickOn(R.string.screen_roomlist_filter_rooms)
        eventsRecorder.assertList(
            listOf(
                RoomListFiltersEvent.ToggleFilter(RoomListFilter.Rooms),
            )
        )
    }

    @Test
    fun `clicking on clear filters generates expected Event`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<RoomListFiltersEvent>()
        setContent {
            RoomListFiltersView(
                state = aRoomListFiltersState(
                    filterSelectionStates = RoomListFilter.entries.map { FilterSelectionState(it, isSelected = true) },
                    eventSink = eventsRecorder
                ),
            )
        }
        pressTag(TestTags.homeScreenClearFilters.value)
        eventsRecorder.assertList(
            listOf(
                RoomListFiltersEvent.ClearSelectedFilters,
            )
        )
    }
}

