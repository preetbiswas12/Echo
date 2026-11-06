/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.echo.features.messages.impl.timeline.aTimelineItemEvent
import com.echo.features.messages.impl.timeline.aTimelineItemReactions
import com.echo.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import com.echo.features.messages.impl.timeline.components.receipt.ReadReceiptViewStateForTimelineItemEventRowPreviewParam
import com.echo.features.messages.impl.timeline.model.TimelineItemReadReceipts
import com.echo.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight

// Note: I add to reduce the size of the fun name, or it does not compile.
// Previous name: TimelineItemEventRowWithSendingStateAndReadReceiptPreview
@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithRRPreview(
    @PreviewParameter(ReadReceiptViewStateForTimelineItemEventRowPreviewParam::class) state: ReadReceiptViewState,
) = ElementPreview {
    Column {
        // A message from someone else
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = false,
                sendState = null,
                content = aTimelineItemTextContent(body = "A message from someone else"),
                timelineItemReactions = aTimelineItemReactions(count = 0),
                readReceiptState = TimelineItemReadReceipts(state.receipts),
            ),
            isLastOutgoingMessage = false,
        )
        // A message from current user
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = true,
                sendState = state.sendState,
                content = aTimelineItemTextContent(body = "A message from me"),
                timelineItemReactions = aTimelineItemReactions(count = 0),
                readReceiptState = TimelineItemReadReceipts(state.receipts),
            ),
            isLastOutgoingMessage = false,
        )
        // Another message from current user
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = true,
                sendState = state.sendState,
                content = aTimelineItemTextContent(body = "A last message from me"),
                timelineItemReactions = aTimelineItemReactions(count = 0),
                readReceiptState = TimelineItemReadReceipts(state.receipts),
            ),
            isLastOutgoingMessage = true,
        )
    }
}

