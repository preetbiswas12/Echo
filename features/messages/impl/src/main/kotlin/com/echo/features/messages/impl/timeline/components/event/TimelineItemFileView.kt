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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.messages.impl.timeline.model.event.TimelineItemFileContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemFileContentPreviewParam
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import com.echo.libraries.ui.common.layout.ContentAvoidingLayoutData
import com.echo.libraries.ui.strings.CommonStrings

/**
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2019-6477&t=2yr7kvVEdtsP4p26-4
 */
@Composable
fun TimelineItemFileView(
    content: TimelineItemFileContent,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    contentValidationValue: ContentValidationValue,
    modifier: Modifier = Modifier,
) {
    TimelineItemAttachmentView(
        icon = CompoundIcons.Attachment(),
        iconContentDescription = stringResource(CommonStrings.common_file),
        filename = content.filename,
        fileExtensionAndSize = content.fileExtensionAndSize,
        onContentLayoutChange = onContentLayoutChange,
        modifier = modifier,
        contentValidationValue = contentValidationValue,
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemFileViewPreview(@PreviewParameter(TimelineItemFileContentPreviewParam::class) content: TimelineItemFileContent) {
    ElementTimelineItemPreview {
        TimelineItemFileView(
            content,
            onContentLayoutChange = {},
            contentValidationValue = ContentValidationValue.Valid,
        )
    }
}
