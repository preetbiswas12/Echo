/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.model.event

import com.echo.libraries.htmlrenderer.api.DocumentNode
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.libraries.mediaviewer.api.helper.formatFileExtensionAndSize
import kotlin.time.Duration

data class TimelineItemAudioContent(
    override val filename: String,
    override val fileSize: Long?,
    override val caption: String?,
    override val formattedCaption: CharSequence?,
    override val htmlCaption: String? = null,
    override val captionTree: DocumentNode? = null,
    override val isEdited: Boolean,
    val duration: Duration,
    override val mediaSource: MediaSource,
    override val mimeType: String,
    override val formattedFileSize: String,
    override val fileExtension: String,
) : TimelineItemEventContentWithAttachment {
    val fileExtensionAndSize =
        formatFileExtensionAndSize(
            fileExtension,
            formattedFileSize
        )
    override val type: String = "TimelineItemAudioContent"
}

