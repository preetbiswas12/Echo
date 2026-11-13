/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.factories.event

import com.echo.features.messages.impl.timeline.model.event.TimelineItemEventContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemProfileChangeContent
import com.echo.libraries.core.extensions.orEmpty
import com.echo.libraries.eventformatter.api.TimelineEventFormatter
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.timeline.item.event.EventContent
import dev.zacsweers.metro.Inject

@Inject
class TimelineItemContentProfileChangeFactory(
    private val timelineEventFormatter: TimelineEventFormatter,
) {
    fun create(content: EventContent, isOutgoing: Boolean, sender: UserId, senderDisambiguatedDisplayName: String): TimelineItemEventContent {
        val text = timelineEventFormatter.format(content, isOutgoing, sender, senderDisambiguatedDisplayName)
        return TimelineItemProfileChangeContent(text.orEmpty().toString())
    }
}
