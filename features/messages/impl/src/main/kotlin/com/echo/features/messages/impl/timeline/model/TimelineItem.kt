/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.model

import androidx.compose.runtime.Immutable
import com.echo.features.messages.impl.timeline.components.MessageShieldData
import com.echo.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemEventContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemImageContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import com.echo.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import com.echo.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import com.echo.libraries.designsystem.components.avatar.AvatarData
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.SendHandle
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.matrix.api.core.TransactionId
import com.echo.libraries.matrix.api.core.UniqueId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.timeline.item.SendTarget
import com.echo.libraries.matrix.api.timeline.item.ThreadSummary
import com.echo.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import com.echo.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import com.echo.libraries.matrix.api.timeline.item.event.LocalEventSendState
import com.echo.libraries.matrix.api.timeline.item.event.MessageShield
import com.echo.libraries.matrix.api.timeline.item.event.MessageShieldProvider
import com.echo.libraries.matrix.api.timeline.item.event.ProfileDetails
import com.echo.libraries.matrix.api.timeline.item.event.SendHandleProvider
import com.echo.libraries.matrix.api.timeline.item.event.TimelineItemDebugInfoProvider
import com.echo.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
import com.echo.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import com.echo.libraries.matrix.ui.messages.reply.InReplyToDetails
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface TimelineItem {
    fun identifier(): UniqueId = when (this) {
        is Event -> id
        is Virtual -> id
        is GroupedEvents -> id
    }

    fun isEvent(eventId: EventId?): Boolean {
        if (eventId == null) return false
        return when (this) {
            is Event -> this.eventId == eventId
            else -> false
        }
    }

    fun contentType(): String = when (this) {
        is Event -> content.type
        is Virtual -> model.type
        is GroupedEvents -> "groupedEvent"
    }

    fun formattedDate(): String? = when (this) {
        is Event -> sentDate.takeIf { it.isNotEmpty() }
        is Virtual -> (model as? TimelineItemDaySeparatorModel)?.formattedDate?.takeIf { it.isNotEmpty() }
        is GroupedEvents -> null
    }

    data class Virtual(
        val id: UniqueId,
        val model: TimelineItemVirtualModel
    ) : TimelineItem

    data class Event(
        val id: UniqueId,
        // Note: eventId can be null when the event is a local echo
        val eventId: EventId? = null,
        val transactionId: TransactionId? = null,
        val senderId: UserId,
        val senderProfile: ProfileDetails,
        val senderAvatar: AvatarData,
        val content: TimelineItemEventContent,
        val sentTimeMillis: Long = 0L,
        val sentTime: String = "",
        val sentDate: String = "",
        val isMine: Boolean = false,
        val isEditable: Boolean,
        val canBeRepliedTo: Boolean,
        val groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
        val reactionsState: TimelineItemReactions,
        val readReceiptState: TimelineItemReadReceipts,
        val localSendState: LocalEventSendState?,
        private val editSendState: LocalEventSendState?,
        private val redactionSendState: LocalEventSendState?,
        val inReplyTo: InReplyToDetails?,
        val threadInfo: TimelineItemThreadInfo?,
        val origin: TimelineItemEventOrigin?,
        val timelineItemDebugInfoProvider: TimelineItemDebugInfoProvider,
        val messageShieldProvider: MessageShieldProvider,
        val sendHandleProvider: SendHandleProvider,
        /**
         * If the keys to this message were forwarded by another user via history sharing (MSC4268), the ID of that user.
         * If this is non-null, then [messageShieldProvider] will also return [MessageShield.AuthenticityNotGuaranteed].
         */
        val forwarder: UserId?,
        /** If [forwarder] is set, the profile of the forwarding user, if it was cached at the time the `EventTimelineItem` was created. */
        val forwarderProfile: ProfileDetails?,
    ) : TimelineItem {
        val showSenderInformation = groupPosition.isNew() && !isMine

        val safeSenderName: String = senderProfile.getDisambiguatedDisplayName(senderId)

        val failedToSend: Boolean = listOf(localSendState, editSendState, redactionSendState).any { it is LocalEventSendState.Failed }

        val isTextMessage: Boolean = content is TimelineItemTextBasedContent

        val isSticker: Boolean = content is TimelineItemStickerContent

        val isRemote = eventId != null

        /** Whether a click on any part of the event bubble should trigger the 'onContentClick' callback.
         *
         *  This is `true` for all events except for visual media events with a caption or formatted caption.
         */
        val isWholeContentClickable = when (content) {
            is TimelineItemStickerContent -> content.formattedCaption == null && content.caption == null
            is TimelineItemImageContent -> content.formattedCaption == null && content.caption == null
            is TimelineItemVideoContent -> content.formattedCaption == null && content.caption == null
            is TimelineItemGalleryContent -> false
            is TimelineItemAttachmentsContent -> false
            else -> true
        }

        val eventOrTransactionId: EventOrTransactionId
            get() = EventOrTransactionId.from(eventId = eventId, transactionId = transactionId)

        // No need to be lazy here?
        val messageShield: MessageShieldData? = messageShieldProvider(strict = false)?.let {
            MessageShieldData(it, forwarder, forwarderProfile)
        }

        val debugInfo: TimelineItemDebugInfo
            get() = timelineItemDebugInfoProvider()

        val sendhandle: SendHandle? get() = sendHandleProvider()

        /**
         * Returns the first pending send state, if any, along with the corresponding [SendTarget].
         */
        fun pendingSend(): Pair<SendTarget, LocalEventSendState>? {
            return when {
                localSendState != null -> SendTarget.Event to localSendState
                editSendState != null -> SendTarget.Edit to editSendState
                redactionSendState != null -> SendTarget.Redaction to redactionSendState
                else -> null
            }
        }
    }

    data class GroupedEvents(
        val id: UniqueId,
        val events: ImmutableList<Event>,
        val aggregatedReadReceipts: ImmutableList<ReadReceiptData>,
    ) : TimelineItem
}

sealed interface TimelineItemThreadInfo {
    data class ThreadRoot(val summary: ThreadSummary, val latestEventText: String?) : TimelineItemThreadInfo
    data class ThreadResponse(val threadRootId: ThreadId) : TimelineItemThreadInfo
}
