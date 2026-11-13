/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.textcomposer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.libraries.matrix.ui.messages.reply.InReplyToDetailsPreviewParam
import com.echo.libraries.textcomposer.model.MessageComposerMode

class MessageComposerModeSpecialPreviewParam : PreviewParameterProvider<MessageComposerMode.Special> {
    override val values: Sequence<MessageComposerMode.Special> = sequenceOf(
        aMessageComposerModeEdit()
    ) +
        // Keep only 3 values from InReplyToDetailsPreviewParam
        InReplyToDetailsPreviewParam().values.take(3).map {
            aMessageComposerModeReply(
                replyToDetails = it
            )
        }
}
