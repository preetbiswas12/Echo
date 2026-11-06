/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.poll.impl.history

import com.echo.features.poll.impl.history.model.PollHistoryFilter
import com.echo.libraries.matrix.api.core.EventId

sealed interface PollHistoryEvent {
    data object LoadMore : PollHistoryEvent
    data class SendPollResponse(val pollStartId: EventId, val answerIds: List<String>) : PollHistoryEvent
    data class EndPoll(val pollStartId: EventId) : PollHistoryEvent
    data class SelectFilter(val filter: PollHistoryFilter) : PollHistoryEvent
}

