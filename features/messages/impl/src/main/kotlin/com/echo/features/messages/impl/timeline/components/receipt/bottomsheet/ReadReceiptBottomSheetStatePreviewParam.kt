/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components.receipt.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.features.messages.impl.timeline.aTimelineItemEvent
import com.echo.features.messages.impl.timeline.components.receipt.ReadReceiptViewStatePreviewParam
import com.echo.features.messages.impl.timeline.model.TimelineItemReadReceipts
import com.echo.libraries.matrix.api.timeline.item.event.LocalEventSendState

class ReadReceiptBottomSheetStatePreviewParam : PreviewParameterProvider<ReadReceiptBottomSheetState> {
    // Reuse the provider ReadReceiptViewStatePreviewParam
    private val readReceiptViewStateProvider = ReadReceiptViewStatePreviewParam()
    override val values: Sequence<ReadReceiptBottomSheetState> = readReceiptViewStateProvider.values
        .filter { it.sendState is LocalEventSendState.Sent && it.receipts.isNotEmpty() }
        .map { readReceiptViewState ->
            ReadReceiptBottomSheetState(
                selectedEvent = aTimelineItemEvent(
                    readReceiptState = TimelineItemReadReceipts(
                        receipts = readReceiptViewState.receipts,
                    )
                ),
                eventSink = {},
            )
        }
}
