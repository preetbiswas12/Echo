/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.notificationsettings

import com.echo.libraries.matrix.api.room.RoomNotificationMode
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiNotificationSettings
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.tests.testutils.testCoroutineDispatchers
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.NotificationSettings

class RustNotificationSettingsServiceTest {
    @Test
    fun test() = runTest {
        val sut = createRustNotificationSettingsService()
        val result = sut.getRoomNotificationSettings(
            roomId = A_ROOM_ID,
            isEncrypted = true,
            isOneToOne = true,
        ).getOrNull()!!
        assertThat(result.mode).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
        assertThat(result.isDefault).isTrue()
    }

    private fun TestScope.createRustNotificationSettingsService(
        notificationSettings: NotificationSettings = FakeFfiNotificationSettings(),
    ) = RustNotificationSettingsService(
        client = FakeFfiClient(
            notificationSettings = notificationSettings,
        ),
        sessionCoroutineScope = this,
        dispatchers = testCoroutineDispatchers(),
    )
}
