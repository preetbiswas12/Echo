/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.timeline.postprocessor

import com.echo.libraries.matrix.api.core.UniqueId
import com.echo.libraries.matrix.api.timeline.MatrixTimelineItem
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import com.echo.libraries.matrix.test.AN_EVENT_ID
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LastForwardIndicatorsPostProcessorTest {
    @Test
    fun `LastForwardIndicatorsPostProcessor does not alter the items with mode not FOCUSED_ON_EVENT`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.Live)
        val result = sut.process(listOf(messageEvent))
        assertThat(result).containsExactly(messageEvent)
    }

    @Test
    fun `LastForwardIndicatorsPostProcessor add virtual items`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.FocusedOnEvent(AN_EVENT_ID))
        val result = sut.process(listOf(messageEvent))
        assertThat(result).containsExactly(
            messageEvent,
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_${messageEvent.uniqueId}"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            )
        )
    }

    @Test
    fun `LastForwardIndicatorsPostProcessor add virtual items on empty list`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.FocusedOnEvent(AN_EVENT_ID))
        val result = sut.process(listOf())
        assertThat(result).containsExactly(
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_fake_id"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            )
        )
    }

    @Test
    fun `LastForwardIndicatorsPostProcessor add virtual items but does not alter the list if called a second time`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.FocusedOnEvent(AN_EVENT_ID))
        // Process a first time
        sut.process(listOf(messageEvent))
        // Process a second time with the same Event
        val result = sut.process(listOf(messageEvent))
        assertThat(result).containsExactly(
            messageEvent,
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_${messageEvent.uniqueId}"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            )
        )
    }

    @Test
    fun `LastForwardIndicatorsPostProcessor add virtual items each time it is called with new Events`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.FocusedOnEvent(AN_EVENT_ID))
        // Process a first time
        sut.process(listOf(dayEvent, messageEvent))
        // Process a second time with the same Event
        val result = sut.process(listOf(dayEvent, messageEvent, messageEvent2))
        assertThat(result).containsExactly(
            dayEvent,
            messageEvent,
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_${messageEvent.uniqueId}"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            ),
            messageEvent2,
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_${messageEvent2.uniqueId}"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            )
        )
    }
}
