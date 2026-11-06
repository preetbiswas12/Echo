/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.eventformatter.impl

import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.core.meta.BuildMeta
import com.echo.libraries.di.SessionScope
import com.echo.libraries.eventformatter.api.TimelineEventFormatter
import com.echo.libraries.eventformatter.impl.mode.RenderingMode
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
import com.echo.libraries.matrix.api.timeline.item.event.RedactedContent
import com.echo.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import com.echo.libraries.matrix.api.timeline.item.event.StateContent
import com.echo.libraries.matrix.api.timeline.item.event.StickerContent
import com.echo.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import com.echo.libraries.matrix.api.timeline.item.event.UnknownContent
import com.echo.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.services.toolbox.api.strings.StringProvider

@ContributesBinding(SessionScope::class)
class DefaultTimelineEventFormatter(
    private val sp: StringProvider,
    private val buildMeta: BuildMeta,
    private val roomMembershipContentFormatter: RoomMembershipContentFormatter,
    private val profileChangeContentFormatter: ProfileChangeContentFormatter,
    private val stateContentFormatter: StateContentFormatter,
) : TimelineEventFormatter {
    override fun format(event: EventTimelineItem): CharSequence? {
        val isOutgoing = event.isOwn
        val senderDisambiguatedDisplayName = event.senderProfile.getDisambiguatedDisplayName(event.sender)
        return format(event.content, isOutgoing, event.sender, senderDisambiguatedDisplayName)
    }

    override fun format(content: EventContent, isOutgoing: Boolean, sender: UserId, senderDisambiguatedDisplayName: String): CharSequence? {
        return when (content) {
            is RoomMembershipContent -> {
                roomMembershipContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing)
            }
            is ProfileChangeContent -> {
                profileChangeContentFormatter.format(content, sender, senderDisambiguatedDisplayName, isOutgoing)
            }
            is StateContent -> {
                stateContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing, RenderingMode.Timeline)
            }
            is CallNotifyContent -> {
                sp.getString(CommonStrings.common_call_started)
            }
            is RedactedContent,
            is LegacyCallInviteContent,
            is StickerContent,
            is PollContent,
            is UnableToDecryptContent,
            is MessageContent,
            is FailedToParseMessageLikeContent,
            is FailedToParseStateContent,
            is LiveLocationContent,
            is UnknownContent -> {
                if (buildMeta.isDebuggable) {
                    error("You should not use this formatter for this event content: $content")
                }
                sp.getString(CommonStrings.common_unsupported_event)
            }
        }
    }
}

