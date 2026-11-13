/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.utils.messagesummary

import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.features.messages.impl.timeline.model.event.TimelineItemEventContent

interface MessageSummaryFormatter {
    fun format(event: TimelineItem.Event): String {
        return format(event.content)
    }
    fun format(content: TimelineItemEventContent): String
}
