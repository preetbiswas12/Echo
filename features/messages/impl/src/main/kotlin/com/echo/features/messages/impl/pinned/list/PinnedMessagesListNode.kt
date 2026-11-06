/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.pinned.list

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.features.messages.impl.actionlist.ActionListPresenter
import com.echo.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import com.echo.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.libraries.androidutils.clipboard.ClipboardHelper
import com.echo.libraries.androidutils.system.openUrlInExternalApp
import com.echo.libraries.androidutils.toast.ToastHelper
import com.echo.libraries.architecture.callback
import com.echo.libraries.di.RoomScope
import com.echo.libraries.emoji.api.picker.EmojiPickerRenderer
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.permalink.PermalinkData
import com.echo.libraries.matrix.api.permalink.PermalinkParser
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.libraries.ui.utils.a11y.hasExternalKeyboard
import com.echo.libraries.ui.utils.a11y.isTalkbackActive

@ContributesNode(RoomScope::class)
@AssistedInject
class PinnedMessagesListNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: PinnedMessagesListPresenter.Factory,
    actionListPresenterFactory: ActionListPresenter.Factory,
    private val timelineItemPresenterFactories: TimelineItemPresenterFactories,
    private val permalinkParser: PermalinkParser,
    private val emojiPickerRenderer: EmojiPickerRenderer,
    private val clipboardHelper: ClipboardHelper,
    private val toastHelper: ToastHelper,
) : Node(buildContext, plugins = plugins), PinnedMessagesListNavigator {
    interface Callback : Plugin {
        fun handleEventClick(event: TimelineItem.Event, canUseOverlay: Boolean)
        fun handleGalleryItemClick(event: TimelineItem.Event, galleryItemIndex: Int, canUseOverlay: Boolean)
        fun navigateToRoomMemberDetails(userId: UserId)
        fun viewInTimeline(eventId: EventId)
        fun handlePermalinkClick(data: PermalinkData.RoomLink)
        fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
        fun handleForwardEventClick(eventId: EventId)
        fun navigateToThread(threadRootId: ThreadId)
    }

    private val callback: Callback = callback()
    private val presenter = presenterFactory.create(
        navigator = this,
        actionListPresenter = actionListPresenterFactory.create(
            postProcessor = PinnedMessagesListTimelineActionPostProcessor(),
            timelineMode = Timeline.Mode.PinnedEvents,
        )
    )

    private fun onLinkClick(context: Context, url: String) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                // Open the room member profile, it will fallback to
                // the user profile if the user is not in the room
                callback.navigateToRoomMemberDetails(permalink.userId)
            }
            is PermalinkData.RoomLink -> {
                callback.handlePermalinkClick(permalink)
            }
            is PermalinkData.FallbackLink,
            is PermalinkData.RoomEmailInviteLink -> {
                context.openUrlInExternalApp(url)
            }
        }
    }

    override fun viewInTimeline(eventId: EventId) {
        callback.viewInTimeline(eventId)
    }

    override fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        callback.navigateToEventDebugInfo(eventId, debugInfo)
    }

    override fun forwardEvent(eventId: EventId) {
        callback.handleForwardEventClick(eventId)
    }

    override fun navigateToThread(threadRootId: ThreadId) {
        callback.navigateToThread(threadRootId)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val canUseOverlay = !isTalkbackActive() && !hasExternalKeyboard()
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
        ) {
            val context = LocalContext.current
            val view = LocalView.current
            val state = presenter.present()
            PinnedMessagesListView(
                state = state,
                onBackClick = ::navigateUp,
                onEventClick = {
                    callback.handleEventClick(it, canUseOverlay)
                },
                onGalleryItemClick = { event, index ->
                    callback.handleGalleryItemClick(event, index, canUseOverlay)
                },
                onUserDataClick = { callback.navigateToRoomMemberDetails(it.userId) },
                onLinkClick = { link -> onLinkClick(context, link.url) },
                onLinkLongClick = {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.LONG_PRESS
                    )
                    clipboardHelper.copyPlainText(it.url) {
                        toastHelper.show(CommonStrings.common_copied_to_clipboard)
                    }
                },
                emojiPickerRenderer = emojiPickerRenderer,
                modifier = modifier
            )
        }
    }
}

