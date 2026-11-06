/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.poll.impl.actions

import dev.zacsweers.metro.ContributesBinding
import im.vector.app.features.analytics.plan.PollVote
import com.echo.features.poll.api.actions.SendPollResponseAction
import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.services.analytics.api.AnalyticsService

@ContributesBinding(RoomScope::class)
class DefaultSendPollResponseAction(
    private val analyticsService: AnalyticsService,
) : SendPollResponseAction {
    override suspend fun execute(timeline: Timeline, pollStartId: EventId, answerIds: List<String>): Result<Unit> {
        return timeline.sendPollResponse(
            pollStartId = pollStartId,
            answers = answerIds,
        ).onSuccess {
            analyticsService.capture(PollVote())
        }
    }
}

