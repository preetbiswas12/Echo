/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.location.test

import com.echo.features.location.api.live.ActiveLiveLocationShareManager
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

class FakeActiveLiveLocationShareManager(
    val setupLambda: () -> Unit = { lambdaError() },
    val startShareLambda: (roomId: RoomId, duration: Duration) -> Result<Unit> = { _, _ -> lambdaError() },
    val stopShareLambda: (roomId: RoomId) -> Result<Unit> = { _ -> lambdaError() },
) : ActiveLiveLocationShareManager {
    private val _sharingRoomIds = MutableStateFlow(emptySet<RoomId>())
    override val sharingRoomIds: StateFlow<Set<RoomId>> = _sharingRoomIds

    override suspend fun setup() {
        setupLambda()
    }

    override suspend fun startShare(roomId: RoomId, duration: Duration): Result<Unit> = simulateLongTask {
        startShareLambda(roomId, duration).onSuccess {
            _sharingRoomIds.update {
                it + roomId
            }
        }
    }

    override suspend fun stopShare(roomId: RoomId): Result<Unit> = simulateLongTask {
        stopShareLambda(roomId).onSuccess {
            _sharingRoomIds.update {
                it - roomId
            }
        }
    }
}

