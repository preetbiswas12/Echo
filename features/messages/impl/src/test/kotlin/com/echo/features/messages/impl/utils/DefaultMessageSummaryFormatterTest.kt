/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.utils

import android.content.Context
import com.echo.features.location.api.Location
import com.echo.features.messages.impl.timeline.model.event.RtcNotificationState
import com.echo.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemLocationContent.Mode
import com.echo.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import com.echo.features.messages.impl.utils.messagesummary.DefaultMessageSummaryFormatter
import com.echo.libraries.matrix.api.notification.CallIntent
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.timeline.aProfileDetails
import com.echo.tests.testutils.robolectric.RobolectricTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

class DefaultMessageSummaryFormatterTest : RobolectricTest() {
    private val formatter = DefaultMessageSummaryFormatter(
        RuntimeEnvironment.getApplication() as Context
    )

    @Test
    @Config(qualifiers = "en")
    fun `format call notification started`() {
        val expected = formatter.format(
            TimelineItemRtcNotificationContent(
                callIntent = CallIntent.VIDEO,
                state = RtcNotificationState.Started
            )
        )
        assertThat(expected).isEqualTo("Call started")
    }

    @Test
    @Config(qualifiers = "en")
    fun `format call notification declined by me`() {
        val expected = formatter.format(
            TimelineItemRtcNotificationContent(
                callIntent = CallIntent.VIDEO,
                state = RtcNotificationState.Declined(byMe = true)
            )
        )
        assertThat(expected).isEqualTo("You declined a call")
    }

    @Test
    @Config(qualifiers = "en")
    fun `format call notification declined`() {
        val expected = formatter.format(
            TimelineItemRtcNotificationContent(
                callIntent = CallIntent.VIDEO,
                state = RtcNotificationState.Declined(byMe = false)
            )
        )
        assertThat(expected).isEqualTo("Call declined")
    }

    @Test
    @Config(qualifiers = "en")
    fun `format live location`() {
        val expected = formatter.format(
            aLocationContent(isLive = true)
        )
        assertThat(expected).isEqualTo("Shared live location")
    }

    @Test
    @Config(qualifiers = "en")
    fun `format static location`() {
        val expected = formatter.format(
            aLocationContent(isLive = false)
        )
        assertThat(expected).isEqualTo("Shared location")
    }
}

private fun aLocationContent(isLive: Boolean) = TimelineItemLocationContent(
    senderId = A_USER_ID,
    senderProfile = aProfileDetails(),
    description = null,
    assetType = null,
    mode = if (isLive) {
        Mode.Live(
            lastKnownLocation = Location.fromGeoUri("geo:1,5"),
            isActive = true,
            endsAt = "",
            endTimestamp = 0,
            isOwnUser = true,
        )
    } else {
        Mode.Static(
            location = Location.fromGeoUri("geo:1,5")!!,
        )
    }
)
