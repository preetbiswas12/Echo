/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl.datasource

import com.google.common.truth.Truth.assertThat
import com.echo.libraries.dateformatter.api.DateFormatter
import com.echo.libraries.dateformatter.test.FakeDateFormatter
import com.echo.libraries.eventformatter.api.RoomLatestEventFormatter
import com.echo.libraries.eventformatter.test.FakeRoomLatestEventFormatter
import com.echo.libraries.matrix.test.room.aRoomInfo
import com.echo.libraries.matrix.test.room.aRoomSummary
import org.junit.Test

class RoomListRoomSummaryFactoryTest {
    @Test
    fun `a room with two members does not prefix its preview with the sender`() {
        val formatter = FakeRoomLatestEventFormatter()
        val factory = aRoomListRoomSummaryFactory(roomLatestEventFormatter = formatter)

        factory.create(aRoomSummary(info = aRoomInfo(isDm = false, activeMembersCount = 2)))

        assertThat(formatter.lastIsDmRoom).isTrue()
    }

    @Test
    fun `a room with more than two members prefixes its preview with the sender`() {
        val formatter = FakeRoomLatestEventFormatter()
        val factory = aRoomListRoomSummaryFactory(roomLatestEventFormatter = formatter)

        factory.create(aRoomSummary(info = aRoomInfo(isDm = false, activeMembersCount = 3)))

        assertThat(formatter.lastIsDmRoom).isFalse()
    }
}

fun aRoomListRoomSummaryFactory(
    dateFormatter: DateFormatter = FakeDateFormatter { _, _, _ -> "Today" },
    roomLatestEventFormatter: RoomLatestEventFormatter = FakeRoomLatestEventFormatter(),
) = RoomListRoomSummaryFactory(
    dateFormatter = dateFormatter,
    roomLatestEventFormatter = roomLatestEventFormatter,
)

