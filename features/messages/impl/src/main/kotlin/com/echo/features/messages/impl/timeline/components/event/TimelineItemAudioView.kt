/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemAudioContentPreviewParam
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import com.echo.libraries.ui.common.layout.ContentAvoidingLayoutData

@Composable
fun TimelineItemAudioView(
    content: TimelineItemAudioContent,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    contentValidationValue: ContentValidationValue,
    modifier: Modifier = Modifier,
) {
    TimelineItemAttachmentView(
        icon = CompoundIcons.Audio(),
        iconContentDescription = null,
        filename = content.filename,
        fileExtensionAndSize = content.fileExtensionAndSize,
        onContentLayoutChange = onContentLayoutChange,
        modifier = modifier,
        contentValidationValue = contentValidationValue,
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemAudioViewPreview(@PreviewParameter(TimelineItemAudioContentPreviewParam::class) content: TimelineItemAudioContent) =
    ElementTimelineItemPreview {
        TimelineItemAudioView(
            content,
            onContentLayoutChange = {},
            contentValidationValue = ContentValidationValue.Valid,
        )
    }

