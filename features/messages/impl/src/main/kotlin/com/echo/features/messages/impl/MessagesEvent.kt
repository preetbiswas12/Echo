/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl

import com.echo.features.messages.impl.actionlist.model.TimelineItemAction
import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import com.echo.libraries.matrix.api.user.MatrixUser

sealed interface MessagesEvent {
    data class HandleAction(val action: TimelineItemAction, val event: TimelineItem.Event) : MessagesEvent
    data class ToggleReaction(val emoji: String, val eventOrTransactionId: EventOrTransactionId) : MessagesEvent
    data class InviteDialogDismissed(val action: InviteDialogAction) : MessagesEvent
    data class OnUserClicked(val user: MatrixUser) : MessagesEvent
    data class ConfirmRedact(val reason: String?) : MessagesEvent
    data object CancelRedact : MessagesEvent
    data object StopLiveLocationShare : MessagesEvent
    data object ShowLiveLocationShare : MessagesEvent
    data object MarkAsFullyReadAndExit : MessagesEvent
    data object ConfirmSelectionAction : MessagesEvent
}

enum class InviteDialogAction {
    Cancel,
    Invite,
}
