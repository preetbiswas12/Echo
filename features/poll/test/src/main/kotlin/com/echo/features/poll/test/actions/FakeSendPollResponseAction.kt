/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.poll.test.actions

import com.echo.features.poll.api.actions.SendPollResponseAction
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.timeline.Timeline

class FakeSendPollResponseAction : SendPollResponseAction {
    private var executionCount = 0

    fun verifyExecutionCount(count: Int) {
        assert(executionCount == count)
    }

    override suspend fun execute(timeline: Timeline, pollStartId: EventId, answerIds: List<String>): Result<Unit> {
        executionCount++
        return Result.success(Unit)
    }
}

