/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.factories.event

import com.echo.features.location.api.Location
import com.echo.features.messages.impl.timeline.model.event.RtcNotificationState
import com.echo.features.messages.impl.timeline.model.event.TimelineItemEventContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import com.echo.libraries.dateformatter.api.DateFormatter
import com.echo.libraries.dateformatter.api.DateFormatterMode
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.timeline.item.event.CallNotifyContent
import com.echo.libraries.matrix.api.timeline.item.event.EventContent
import com.echo.libraries.matrix.api.timeline.item.event.EventTimelineItem
import com.echo.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import com.echo.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import com.echo.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import com.echo.libraries.matrix.api.timeline.item.event.LiveLocationContent
import com.echo.libraries.matrix.api.timeline.item.event.MessageContent
import com.echo.libraries.matrix.api.timeline.item.event.PollContent
import com.echo.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import com.echo.libraries.matrix.api.timeline.item.event.ProfileDetails
import com.echo.libraries.matrix.api.timeline.item.event.RedactedContent
import com.echo.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import com.echo.libraries.matrix.api.timeline.item.event.StateContent
import com.echo.libraries.matrix.api.timeline.item.event.StickerContent
import com.echo.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import com.echo.libraries.matrix.api.timeline.item.event.UnknownContent
import com.echo.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.services.toolbox.api.strings.StringProvider
import dev.zacsweers.metro.Inject

@Inject
class TimelineItemContentFactory(
    private val messageFactory: TimelineItemContentMessageFactory,
    private val redactedMessageFactory: TimelineItemContentRedactedFactory,
    private val stickerFactory: TimelineItemContentStickerFactory,
    private val pollFactory: TimelineItemContentPollFactory,
    private val utdFactory: TimelineItemContentUTDFactory,
    private val roomMembershipFactory: TimelineItemContentRoomMembershipFactory,
    private val profileChangeFactory: TimelineItemContentProfileChangeFactory,
    private val stateFactory: TimelineItemContentStateFactory,
    private val failedToParseMessageFactory: TimelineItemContentFailedToParseMessageFactory,
    private val failedToParseStateFactory: TimelineItemContentFailedToParseStateFactory,
    private val sessionId: SessionId,
    private val dateFormatter: DateFormatter,
    private val stringProvider: StringProvider,
) {
    suspend fun create(eventTimelineItem: EventTimelineItem): TimelineItemEventContent {
        return create(
            itemContent = eventTimelineItem.content,
            eventId = eventTimelineItem.eventId,
            isEditable = eventTimelineItem.isEditable,
            sender = eventTimelineItem.sender,
            senderProfile = eventTimelineItem.senderProfile,
        )
    }

    suspend fun create(
        itemContent: EventContent,
        eventId: EventId?,
        isEditable: Boolean,
        sender: UserId,
        senderProfile: ProfileDetails,
    ): TimelineItemEventContent {
        val isOutgoing = sessionId == sender
        return when (itemContent) {
            is FailedToParseMessageLikeContent -> failedToParseMessageFactory.create(itemContent)
            is FailedToParseStateContent -> failedToParseStateFactory.create(itemContent)
            is MessageContent -> {
                messageFactory.create(
                    senderId = sender,
                    senderProfile = senderProfile,
                    content = itemContent,
                    eventId = eventId,
                )
            }
            is ProfileChangeContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                profileChangeFactory.create(itemContent, isOutgoing, sender, senderDisambiguatedDisplayName)
            }
            is RedactedContent -> redactedMessageFactory.create(itemContent)
            is RoomMembershipContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                roomMembershipFactory.create(itemContent, isOutgoing, sender, senderDisambiguatedDisplayName)
            }
            is LegacyCallInviteContent -> TimelineItemLegacyCallInviteContent
            is StateContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                stateFactory.create(itemContent, isOutgoing, sender, senderDisambiguatedDisplayName)
            }
            is StickerContent -> stickerFactory.create(itemContent)
            is PollContent -> pollFactory.create(eventId, isEditable, isOutgoing, itemContent)
            is UnableToDecryptContent -> utdFactory.create(itemContent)
            is CallNotifyContent -> TimelineItemRtcNotificationContent(
                callIntent = itemContent.callIntent,
                state = when {
                    itemContent.activeMembers.isNotEmpty() -> {
                        RtcNotificationState.Active(
                            joinedMemberIds = itemContent.activeMembers,
                            isJoined = itemContent.isJoined,
                            callStartTsMillis = itemContent.callStartTsMillis,
                            callIntent = itemContent.callIntent
                        )
                    }
                    else -> {
                        if (itemContent.declinedBy.isEmpty()) {
                            RtcNotificationState.Started
                        } else {
                            RtcNotificationState.Declined(itemContent.declinedBy.any { it == sessionId })
                        }
                    }
                }
            )
            is UnknownContent -> TimelineItemUnknownContent
            is LiveLocationContent -> {
                val lastKnownLocation = itemContent.locations.mapNotNull { beacon ->
                    Location.fromGeoUri(beacon.geoUri)
                }.lastOrNull()

                val endsAt = dateFormatter.format(
                    timestamp = itemContent.endTimestamp,
                    mode = DateFormatterMode.TimeOnly
                )
                // Always create content, location can be null for "loading/waiting" state
                TimelineItemLocationContent(
                    description = itemContent.description?.trimEnd(),
                    assetType = itemContent.assetType,
                    senderId = sender,
                    senderProfile = senderProfile,
                    mode = TimelineItemLocationContent.Mode.Live(
                        lastKnownLocation = lastKnownLocation,
                        isActive = itemContent.isLive,
                        endsAt = stringProvider.getString(CommonStrings.common_ends_at, endsAt),
                        endTimestamp = itemContent.endTimestamp,
                        isOwnUser = sessionId == sender
                    ),
                )
            }
        }
    }
}
