/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.factories.event

import com.echo.features.messages.impl.timeline.model.event.TimelineItemEventContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import com.echo.libraries.matrix.api.timeline.item.event.RedactedContent
import dev.zacsweers.metro.Inject

@Inject
class TimelineItemContentRedactedFactory {
    fun create(@Suppress("UNUSED_PARAMETER") content: RedactedContent): TimelineItemEventContent {
        return TimelineItemRedactedContent
    }
}
