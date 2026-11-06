/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
package com.echo.libraries.matrix.impl.roomlist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.echo.libraries.matrix.api.roomlist.RoomListService
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import com.echo.libraries.matrix.impl.room.RoomSyncSubscriber
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicator
import org.matrix.rustcomponents.sdk.RoomListService as RustRoomListService

@OptIn(ExperimentalCoroutinesApi::class)
class RustBaseRoomListServiceTest {
    @Test
    fun `syncIndicator should emit the expected values`() = runTest {
        val roomListService = FakeFfiRoomListService()
        val sut = createRustRoomListService(
            roomListService = roomListService,
        )
        // Give time for mxCallback to setup
        runCurrent()
        sut.syncIndicator.test {
            assertThat(awaitItem()).isEqualTo(RoomListService.SyncIndicator.Hide)
            roomListService.emitRoomListServiceSyncIndicator(RoomListServiceSyncIndicator.SHOW)
            assertThat(awaitItem()).isEqualTo(RoomListService.SyncIndicator.Show)
            roomListService.emitRoomListServiceSyncIndicator(RoomListServiceSyncIndicator.HIDE)
            assertThat(awaitItem()).isEqualTo(RoomListService.SyncIndicator.Hide)
        }
    }
}

private fun TestScope.createRustRoomListService(
    roomListService: RustRoomListService = FakeFfiRoomListService(),
) = RustRoomListService(
    innerRoomListService = roomListService,
    sessionDispatcher = StandardTestDispatcher(testScheduler),
    roomListFactory = RoomListFactory(
        innerRoomListService = roomListService,
        analyticsService = FakeAnalyticsService(),
    ),
    roomSyncSubscriber = RoomSyncSubscriber(
        roomListService = roomListService,
        dispatchers = testCoroutineDispatchers(),
    ),
    sessionCoroutineScope = backgroundScope,
)

