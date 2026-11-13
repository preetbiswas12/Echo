/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.eventformatter.impl

import com.echo.libraries.core.extensions.DEFAULT_SAFE_LENGTH
import com.echo.libraries.di.SessionScope
import com.echo.libraries.eventformatter.api.RoomLatestEventFormatter
import com.echo.libraries.eventformatter.impl.mode.RenderingMode
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.permalink.PermalinkParser
import com.echo.libraries.matrix.api.roomlist.LatestEventValue
import com.echo.libraries.matrix.api.timeline.item.event.AudioMessageType
import com.echo.libraries.matrix.api.timeline.item.event.CallNotifyContent
import com.echo.libraries.matrix.api.timeline.item.event.EmoteMessageType
import com.echo.libraries.matrix.api.timeline.item.event.EventContent
import com.echo.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import com.echo.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import com.echo.libraries.matrix.api.timeline.item.event.FileMessageType
import com.echo.libraries.matrix.api.timeline.item.event.GalleryMessageType
import com.echo.libraries.matrix.api.timeline.item.event.ImageMessageType
import com.echo.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import com.echo.libraries.matrix.api.timeline.item.event.LiveLocationContent
import com.echo.libraries.matrix.api.timeline.item.event.LocationMessageType
import com.echo.libraries.matrix.api.timeline.item.event.MessageContent
import com.echo.libraries.matrix.api.timeline.item.event.MessageType
import com.echo.libraries.matrix.api.timeline.item.event.NoticeMessageType
import com.echo.libraries.matrix.api.timeline.item.event.OtherMessageType
import com.echo.libraries.matrix.api.timeline.item.event.PollContent
import com.echo.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import com.echo.libraries.matrix.api.timeline.item.event.RedactedContent
import com.echo.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import com.echo.libraries.matrix.api.timeline.item.event.StateContent
import com.echo.libraries.matrix.api.timeline.item.event.StickerContent
import com.echo.libraries.matrix.api.timeline.item.event.StickerMessageType
import com.echo.libraries.matrix.api.timeline.item.event.TextMessageType
import com.echo.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import com.echo.libraries.matrix.api.timeline.item.event.UnknownContent
import com.echo.libraries.matrix.api.timeline.item.event.VideoMessageType
import com.echo.libraries.matrix.api.timeline.item.event.VoiceMessageType
import com.echo.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import com.echo.libraries.matrix.ui.messages.toPlainText
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.services.toolbox.api.strings.StringProvider
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(SessionScope::class)
class DefaultRoomLatestEventFormatter(
    private val sp: StringProvider,
    private val roomMembershipContentFormatter: RoomMembershipContentFormatter,
    private val profileChangeContentFormatter: ProfileChangeContentFormatter,
    private val stateContentFormatter: StateContentFormatter,
    private val rtcNotificationContentFormatter: RtcNotificationContentFormatter,
    private val permalinkParser: PermalinkParser,
) : RoomLatestEventFormatter {
    override fun format(
        latestEvent: LatestEventValue.Local,
        isDmRoom: Boolean,
    ): CharSequence? = formatContent(
        content = latestEvent.content,
        isDmRoom = isDmRoom,
        isOutgoing = true,
        senderId = latestEvent.senderId,
        senderDisambiguatedDisplayName = latestEvent.senderProfile.getDisambiguatedDisplayName(latestEvent.senderId)
    )

    override fun format(
        latestEvent: LatestEventValue.Remote,
        isDmRoom: Boolean,
    ): CharSequence? = formatContent(
        content = latestEvent.content,
        isDmRoom = isDmRoom,
        isOutgoing = latestEvent.isOwn,
        senderId = latestEvent.senderId,
        senderDisambiguatedDisplayName = latestEvent.senderProfile.getDisambiguatedDisplayName(latestEvent.senderId)
    )

    private fun formatContent(
        content: EventContent,
        isDmRoom: Boolean,
        isOutgoing: Boolean,
        senderId: UserId,
        senderDisambiguatedDisplayName: String
    ): CharSequence? {
        return when (content) {
            is MessageContent -> content.process(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            is RedactedContent -> {
                val message = sp.getString(CommonStrings.common_message_removed)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is StickerContent -> {
                content.bestDescription.prefixWith(sp.getString(CommonStrings.common_sticker))
                    .prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is UnableToDecryptContent -> {
                val message = sp.getString(CommonStrings.common_waiting_for_decryption_key)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is RoomMembershipContent -> {
                roomMembershipContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing)
            }
            is ProfileChangeContent -> {
                profileChangeContentFormatter.format(content, senderId, senderDisambiguatedDisplayName, isOutgoing)
            }
            is StateContent -> {
                stateContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing, RenderingMode.RoomList)
            }
            is PollContent -> {
                content.question.prefixWith(sp.getString(CommonStrings.common_poll_summary_prefix))
                    .prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is FailedToParseMessageLikeContent, is FailedToParseStateContent, is UnknownContent -> {
                val message = sp.getString(CommonStrings.common_unsupported_event)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is LiveLocationContent -> {
                val message = sp.getString(CommonStrings.common_shared_live_location)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is LegacyCallInviteContent -> sp.getString(CommonStrings.common_unsupported_call)
            is CallNotifyContent -> rtcNotificationContentFormatter.format(content, isDmRoom)
        }?.take(DEFAULT_SAFE_LENGTH)
    }

    private fun MessageContent.process(
        senderDisambiguatedDisplayName: String,
        isDmRoom: Boolean,
        isOutgoing: Boolean
    ): CharSequence {
        val message = when (val messageType: MessageType = type) {
            // Doesn't need a prefix
            is EmoteMessageType -> {
                return "* ${senderDisambiguatedDisplayName.bidiIsolate()} ${messageType.body}"
            }
            is TextMessageType -> {
                messageType.toPlainText(permalinkParser)
            }
            is VideoMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_video))
            }
            is ImageMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_image))
            }
            is StickerMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_sticker))
            }
            is LocationMessageType -> {
                sp.getString(CommonStrings.common_shared_location)
            }
            is FileMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_file))
            }
            is AudioMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_audio))
            }
            is VoiceMessageType -> {
                messageType
                    .toPlainText(permalinkParser, "")
                    .takeIf { it.isNotEmpty() }
                    ?.prefixWith(sp.getString(CommonStrings.common_voice_message))
                    ?: sp.getString(CommonStrings.common_voice_message)
            }
            is OtherMessageType -> {
                messageType.body
            }
            is GalleryMessageType -> {
                messageType.body.prefixWith(sp.getString(CommonStrings.common_gallery))
            }
            is NoticeMessageType -> {
                messageType.body
            }
        }
        return message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
    }

    private fun CharSequence.prefixIfNeeded(
        senderDisambiguatedDisplayName: String,
        isDmRoom: Boolean,
        isOutgoing: Boolean,
    ): CharSequence = if (isDmRoom) {
        this
    } else {
        prefixWith(
            if (isOutgoing) {
                sp.getString(CommonStrings.common_you)
            } else {
                senderDisambiguatedDisplayName.bidiIsolate()
            }
        )
    }
}
