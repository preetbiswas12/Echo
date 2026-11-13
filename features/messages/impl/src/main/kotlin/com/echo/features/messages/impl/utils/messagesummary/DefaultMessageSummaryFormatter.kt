/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.utils.messagesummary

import android.content.Context
import com.echo.features.messages.impl.R
import com.echo.features.messages.impl.timeline.model.event.RtcNotificationState
import com.echo.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemEventContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemFileContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemImageContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemPollContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemProfileChangeContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemStateContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import com.echo.libraries.core.extensions.toSafeLength
import com.echo.libraries.di.RoomScope
import com.echo.libraries.di.annotations.ApplicationContext
import com.echo.libraries.ui.strings.CommonStrings
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(RoomScope::class)
class DefaultMessageSummaryFormatter(
    @ApplicationContext private val context: Context,
) : MessageSummaryFormatter {
    override fun format(content: TimelineItemEventContent): String {
        return when (content) {
            is TimelineItemTextBasedContent -> content.plainText
            is TimelineItemProfileChangeContent -> content.body
            is TimelineItemStateContent -> content.body
            is TimelineItemLocationContent -> when (content.mode) {
                is TimelineItemLocationContent.Mode.Live -> context.getString(CommonStrings.common_shared_live_location)
                is TimelineItemLocationContent.Mode.Static -> context.getString(CommonStrings.common_shared_location)
            }
            is TimelineItemEncryptedContent -> context.getString(R.string.common_unable_to_decrypt)
            is TimelineItemRedactedContent -> context.getString(CommonStrings.common_message_removed)
            is TimelineItemPollContent -> content.question
            is TimelineItemVoiceContent -> context.getString(CommonStrings.common_voice_message)
            is TimelineItemUnknownContent -> context.getString(CommonStrings.common_unsupported_event)
            is TimelineItemImageContent -> context.getString(CommonStrings.common_image)
            is TimelineItemStickerContent -> context.getString(CommonStrings.common_sticker)
            is TimelineItemVideoContent -> context.getString(CommonStrings.common_video)
            is TimelineItemFileContent -> context.getString(CommonStrings.common_file)
            is TimelineItemAudioContent -> context.getString(CommonStrings.common_audio)
            is TimelineItemGalleryContent -> context.getString(CommonStrings.common_gallery)
            is TimelineItemAttachmentsContent -> {
                val count = content.attachments.size
                val extensions = content.attachments.take(3).joinToString { it.fileExtension }
                if (count <= 3) {
                    extensions
                } else {
                    "$extensions +${count - 3}"
                }
            }
            is TimelineItemLegacyCallInviteContent -> context.getString(CommonStrings.common_unsupported_call)
            is TimelineItemRtcNotificationContent -> when (content.state) {
                is RtcNotificationState.Declined -> {
                    if (content.state.byMe) {
                        context.getString(CommonStrings.common_call_you_declined)
                    } else {
                        context.getString(CommonStrings.common_call_declined)
                    }
                }
                RtcNotificationState.Started, is RtcNotificationState.Active
                    -> context.getString(CommonStrings.common_call_started)
            }
        }
            // Truncate the message to a safe length to avoid crashes in Compose
            .toSafeLength()
    }
}
