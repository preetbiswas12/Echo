/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.protection

import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationState

sealed interface TimelineProtectionEvent {
    data class ShowContent(val eventId: EventId?) : TimelineProtectionEvent
    data class ValidateContent(
        val mediaSources: List<MediaSource>,
        val validationState: ContentValidationState,
    ) : TimelineProtectionEvent
}
