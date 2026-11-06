/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.model.event

import com.echo.features.poll.api.pollcontent.PollAnswerItem
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.poll.PollKind

data class TimelineItemPollContent(
    val isMine: Boolean,
    val isEditable: Boolean,
    val eventId: EventId?,
    val question: String,
    val answerItems: List<PollAnswerItem>,
    val pollKind: PollKind,
    val maxSelections: ULong,
    val isEnded: Boolean,
    override val isEdited: Boolean,
) : TimelineItemEventContent,
    TimelineItemEventMutableContent {
    override val type: String = "TimelineItemPollContent"
}

