/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.factories.event

import com.echo.features.messages.impl.timeline.factories.TimelineItemsFactoryConfig
import com.echo.features.messages.impl.timeline.groups.canBeDisplayedInBubbleBlock
import com.echo.features.messages.impl.timeline.model.AggregatedReaction
import com.echo.features.messages.impl.timeline.model.AggregatedReactionSender
import com.echo.features.messages.impl.timeline.model.ReadReceiptData
import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.features.messages.impl.timeline.model.TimelineItemGroupPosition
import com.echo.features.messages.impl.timeline.model.TimelineItemReactions
import com.echo.features.messages.impl.timeline.model.TimelineItemReadReceipts
import com.echo.features.messages.impl.timeline.model.TimelineItemThreadInfo
import com.echo.features.messages.impl.utils.messagesummary.MessageSummaryFormatter
import com.echo.libraries.core.bool.orTrue
import com.echo.libraries.dateformatter.api.DateFormatter
import com.echo.libraries.dateformatter.api.DateFormatterMode
import com.echo.libraries.designsystem.components.avatar.AvatarData
import com.echo.libraries.designsystem.components.avatar.AvatarSize
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.permalink.PermalinkParser
import com.echo.libraries.matrix.api.timeline.MatrixTimelineItem
import com.echo.libraries.matrix.api.timeline.item.EventThreadInfo
import com.echo.libraries.matrix.api.timeline.item.event.getAvatarUrl
import com.echo.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import com.echo.libraries.matrix.ui.messages.reply.map
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private const val GROUPING_TIMEOUT_MS = 5 * 60 * 1000L

@AssistedInject
class TimelineItemEventFactory(
    @Assisted private val config: TimelineItemsFactoryConfig,
    private val contentFactory: TimelineItemContentFactory,
    private val matrixClient: MatrixClient,
    private val dateFormatter: DateFormatter,
    private val permalinkParser: PermalinkParser,
    private val summaryFormatter: MessageSummaryFormatter,
) {
    @AssistedFactory
    interface Creator {
        fun create(config: TimelineItemsFactoryConfig): TimelineItemEventFactory
    }

    suspend fun create(
        currentTimelineItem: MatrixTimelineItem.Event,
        index: Int,
        timelineItems: List<MatrixTimelineItem>,
    ): TimelineItem.Event {
        val currentSender = currentTimelineItem.event.sender
        val groupPosition =
            computeGroupPosition(currentTimelineItem, timelineItems, index)
        val senderProfile = currentTimelineItem.event.senderProfile
        val sentTime = dateFormatter.format(
            timestamp = currentTimelineItem.event.timestamp,
            mode = DateFormatterMode.TimeOnly,
        )
        val sentDate = dateFormatter.format(
            timestamp = currentTimelineItem.event.timestamp,
            mode = DateFormatterMode.Day,
            useRelative = true,
        )
        val senderAvatarData = AvatarData(
            id = currentSender.value,
            name = senderProfile.getDisambiguatedDisplayName(currentSender),
            url = senderProfile.getAvatarUrl(),
            size = AvatarSize.TimelineSender
        )
        val mappedThreadInfo = when (val threadInfo = currentTimelineItem.event.threadInfo()) {
            is EventThreadInfo.ThreadResponse -> {
                TimelineItemThreadInfo.ThreadResponse(threadInfo.threadRootId)
            }
            is EventThreadInfo.ThreadRoot -> {
                TimelineItemThreadInfo.ThreadRoot(
                    summary = threadInfo.summary,
                    latestEventText = threadInfo.summary.latestEvent.dataOrNull()
                        ?.let {
                            contentFactory.create(
                                itemContent = it.content,
                                eventId = it.eventOrTransactionId.eventId,
                                isEditable = false,
                                sender = it.senderId,
                                senderProfile = it.senderProfile,
                            )
                        }
                        ?.let(summaryFormatter::format)
                )
            }
            null -> null
        }

        return TimelineItem.Event(
            id = currentTimelineItem.uniqueId,
            eventId = currentTimelineItem.eventId,
            transactionId = currentTimelineItem.transactionId,
            senderId = currentSender,
            senderProfile = senderProfile,
            senderAvatar = senderAvatarData,
            content = contentFactory.create(currentTimelineItem.event),
            isMine = currentTimelineItem.event.isOwn,
            isEditable = currentTimelineItem.event.isEditable,
            canBeRepliedTo = currentTimelineItem.event.canBeRepliedTo,
            sentTimeMillis = currentTimelineItem.event.timestamp,
            sentTime = sentTime,
            sentDate = sentDate,
            groupPosition = groupPosition,
            reactionsState = currentTimelineItem.computeReactionsState(),
            readReceiptState = currentTimelineItem.computeReadReceiptState(),
            localSendState = currentTimelineItem.event.localSendState,
            editSendState = currentTimelineItem.event.editSendState,
            redactionSendState = currentTimelineItem.event.redactionSendState,
            inReplyTo = currentTimelineItem.event.inReplyTo()?.map(permalinkParser = permalinkParser),
            threadInfo = mappedThreadInfo,
            origin = currentTimelineItem.event.origin,
            timelineItemDebugInfoProvider = currentTimelineItem.event.timelineItemDebugInfoProvider,
            messageShieldProvider = currentTimelineItem.event.messageShieldProvider,
            sendHandleProvider = currentTimelineItem.event.sendHandleProvider,
            forwarder = currentTimelineItem.event.forwarder,
            forwarderProfile = currentTimelineItem.event.forwarderProfile,
        )
    }

    private fun MatrixTimelineItem.Event.computeReactionsState(): TimelineItemReactions {
        if (!config.computeReactions) {
            return TimelineItemReactions(reactions = persistentListOf())
        }
        var aggregatedReactions = this.event.reactions.map { reaction ->
            // Sort reactions within an aggregation by timestamp descending.
            // This puts the most recent at the top, useful in cases like the
            // reaction summary view or getting the most recent reaction.
            AggregatedReaction(
                key = reaction.key,
                currentUserId = matrixClient.sessionId,
                senders = reaction.senders
                    .sortedByDescending { it.timestamp }
                    .map {
                        AggregatedReactionSender(
                            senderId = it.senderId,
                            timestamp = it.timestamp,
                            sentTime = dateFormatter.format(
                                it.timestamp,
                                DateFormatterMode.TimeOrDate,
                            ),
                        )
                    }
                    .toImmutableList()
            )
        }
        // Sort aggregated reactions by count and then timestamp ascending, using
        // the most recent reaction in the aggregation(hence index 0).
        // This appends new aggregations on the end of the reaction layout.
        aggregatedReactions = aggregatedReactions
            .sortedWith(
                compareByDescending<AggregatedReaction> { it.count }
                    .thenBy { it.senders[0].timestamp }
            )
        return TimelineItemReactions(
            reactions = aggregatedReactions.toImmutableList()
        )
    }

    private fun MatrixTimelineItem.Event.computeReadReceiptState(): TimelineItemReadReceipts {
        if (!config.computeReadReceipts) {
            return TimelineItemReadReceipts(receipts = persistentListOf())
        }
        return TimelineItemReadReceipts(
            receipts = event.receipts
                .map { receipt ->
                    ReadReceiptData(
                        userId = receipt.userId,
                        formattedDate = dateFormatter.format(
                            receipt.timestamp,
                            mode = DateFormatterMode.TimeOrDate,
                        )
                    )
                }
                .toImmutableList()
        )
    }

    private fun computeGroupPosition(
        currentTimelineItem: MatrixTimelineItem.Event,
        timelineItems: List<MatrixTimelineItem>,
        index: Int
    ): TimelineItemGroupPosition {
        val prevTimelineItem =
            timelineItems.getOrNull(index - 1) as? MatrixTimelineItem.Event
        val nextTimelineItem =
            timelineItems.getOrNull(index + 1) as? MatrixTimelineItem.Event
        val currentSender = currentTimelineItem.event.sender
        val previousSender = prevTimelineItem?.event?.sender
        val nextSender = nextTimelineItem?.event?.sender

        val previousIsGroupable = prevTimelineItem?.let {
            it.canBeDisplayedInBubbleBlock() &&
                currentTimelineItem.event.timestamp - it.event.timestamp <= GROUPING_TIMEOUT_MS
        }.orTrue()
        val nextIsGroupable = nextTimelineItem?.let {
            it.canBeDisplayedInBubbleBlock() &&
                it.event.timestamp - currentTimelineItem.event.timestamp <= GROUPING_TIMEOUT_MS
        }.orTrue()

        return when {
            previousSender != currentSender && nextSender == currentSender -> {
                if (nextIsGroupable) {
                    TimelineItemGroupPosition.First
                } else {
                    TimelineItemGroupPosition.None
                }
            }
            previousSender == currentSender && nextSender == currentSender -> {
                if (previousIsGroupable) {
                    if (nextIsGroupable) {
                        TimelineItemGroupPosition.Middle
                    } else {
                        TimelineItemGroupPosition.Last
                    }
                } else {
                    if (nextIsGroupable) {
                        TimelineItemGroupPosition.First
                    } else {
                        TimelineItemGroupPosition.None
                    }
                }
            }
            // In the following case, we have nextSender != currentSender == true
            previousSender == currentSender -> {
                if (previousIsGroupable) {
                    TimelineItemGroupPosition.Last
                } else {
                    TimelineItemGroupPosition.None
                }
            }
            else -> TimelineItemGroupPosition.None
        }
    }
}
