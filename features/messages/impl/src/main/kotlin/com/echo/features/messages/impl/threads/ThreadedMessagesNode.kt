/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.threads

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.echo.annotations.ContributesNode
import com.echo.compound.theme.ElementTheme
import com.echo.features.messages.impl.MessagesEvent
import com.echo.features.messages.impl.MessagesNavigator
import com.echo.features.messages.impl.MessagesPresenter
import com.echo.features.messages.impl.MessagesState
import com.echo.features.messages.impl.MessagesView
import com.echo.features.messages.impl.actionlist.ActionListPresenter
import com.echo.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor
import com.echo.features.messages.impl.attachments.Attachment
import com.echo.features.messages.impl.messagecomposer.MessageComposerEvent
import com.echo.features.messages.impl.messagecomposer.MessageComposerPresenter
import com.echo.features.messages.impl.timeline.TimelineController
import com.echo.features.messages.impl.timeline.TimelineEvent
import com.echo.features.messages.impl.timeline.TimelinePresenter
import com.echo.features.messages.impl.timeline.TimelineSelectionOwner
import com.echo.features.messages.impl.timeline.components.customreaction.CustomReactionBottomSheet
import com.echo.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import com.echo.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.features.roommembermoderation.api.ModerationAction
import com.echo.features.roommembermoderation.api.RoomMemberModerationEvent
import com.echo.features.roommembermoderation.api.RoomMemberModerationRenderer
import com.echo.libraries.androidutils.browser.openUrlInChromeCustomTab
import com.echo.libraries.androidutils.system.openUrlInExternalApp
import com.echo.libraries.architecture.NodeInputs
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.architecture.callback
import com.echo.libraries.architecture.inputs
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.designsystem.utils.OnLifecycleEvent
import com.echo.libraries.di.RoomScope
import com.echo.libraries.emoji.api.picker.EmojiPickerRenderer
import com.echo.libraries.matrix.api.analytics.toAnalyticsViewRoom
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.core.toRoomIdOrAlias
import com.echo.libraries.matrix.api.permalink.PermalinkData
import com.echo.libraries.matrix.api.permalink.PermalinkParser
import com.echo.libraries.matrix.api.room.CreateTimelineParams
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.libraries.matrix.api.room.alias.matches
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import com.echo.libraries.matrix.ui.model.getBestName
import com.echo.libraries.ui.utils.a11y.hasExternalKeyboard
import com.echo.libraries.ui.utils.a11y.isTalkbackActive
import com.echo.services.analytics.api.AnalyticsService
import com.echo.services.appnavstate.api.AppNavigationStateService
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@ContributesNode(RoomScope::class)
@AssistedInject
class ThreadedMessagesNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val room: JoinedRoom,
    private val analyticsService: AnalyticsService,
    private val messageComposerPresenterFactory: MessageComposerPresenter.Factory,
    private val timelinePresenterFactory: TimelinePresenter.Factory,
    private val presenterFactory: MessagesPresenter.Factory,
    private val actionListPresenterFactory: ActionListPresenter.Factory,
    private val timelineItemPresenterFactories: TimelineItemPresenterFactories,
    private val permalinkParser: PermalinkParser,
    private val appNavigationStateService: AppNavigationStateService,
    private val roomMemberModerationRenderer: RoomMemberModerationRenderer,
    private val emojiPickerRenderer: EmojiPickerRenderer,
    private val dispatchers: CoroutineDispatchers,
) : Node(buildContext, plugins = plugins), MessagesNavigator, TimelineSelectionOwner {
    data class Inputs(
        val threadRootEventId: ThreadId,
        val focusedEventId: EventId?,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val callback: Callback = callback()

    private lateinit var threadedTimeline: Timeline
    private var timelineController: TimelineController? by mutableStateOf(null)
    private var presenter: Presenter<MessagesState>? by mutableStateOf(null)
    private var timelinePresenter: TimelinePresenter? = null

    /**
     * This should be fast to load, but not faster than several UI frames, which will cause ANRs.
     * We'll load the [presenter] in an async way to prevent this.
     */
    private suspend fun createPresenter(): Presenter<MessagesState> {
        threadedTimeline = room.createTimeline(CreateTimelineParams.Threaded(threadRootEventId = inputs.threadRootEventId)).getOrThrow()
        val timelineController = TimelineController(
            room,
            liveTimeline = threadedTimeline,
            roomCoroutineScope = room.roomCoroutineScope,
            dispatchers = dispatchers,
        )
        this.timelineController = timelineController
        val timelinePresenter = timelinePresenterFactory.create(timelineController = timelineController, this)
        this.timelinePresenter = timelinePresenter
        return presenterFactory.create(
            navigator = this,
            composerPresenter = messageComposerPresenterFactory.create(timelineController, this, threadRoot = inputs.threadRootEventId),
            timelinePresenter = timelinePresenter,
            // TODO add special processor for threaded timeline
            actionListPresenter = actionListPresenterFactory.create(
                postProcessor = TimelineItemActionPostProcessor.Default,
                timelineMode = timelineController.mainTimelineMode(),
            ),
            timelineController = timelineController,
        )
    }

    interface Callback : Plugin {
        fun handleEventClick(timelineMode: Timeline.Mode, event: TimelineItem.Event, canUseOverlay: Boolean): Boolean
        fun handleGalleryItemClick(event: TimelineItem.Event, galleryItemIndex: Int, canUseOverlay: Boolean): Boolean
        fun navigateToPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?)
        fun navigateToRoomMemberDetails(userId: UserId)
        fun handlePermalinkClick(data: PermalinkData)
        fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
        fun handleForwardEventClick(eventIds: List<EventId>)
        fun navigateToReportMessage(eventId: EventId, senderId: UserId)
        fun navigateToSendLocation()
        fun navigateToCreatePoll()
        fun navigateToEditPoll(eventId: EventId)
        fun navigateToCurrentLiveLocation()
        fun navigateToRoomCall(roomId: RoomId, isAudioCall: Boolean)
        fun navigateToThread(threadRootId: ThreadId, focusedEventId: EventId?)
        fun navigateToDeveloperSettings()

        fun navigateToAvatarPreview(username: String, avatarUrl: String)
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                analyticsService.capture(room.toAnalyticsViewRoom())
                lifecycleScope.launch {
                    presenter = createPresenter()
                }
            },
            onStart = {
                appNavigationStateService.onNavigateToThread(id, inputs.threadRootEventId)
            },
            onStop = {
                appNavigationStateService.onLeavingThread(id)
            },
            onDestroy = {
                if (this::threadedTimeline.isInitialized) {
                    threadedTimeline.close()
                }
                timelineController?.close()
            }
        )
    }

    private fun onLinkClick(
        activity: Activity,
        darkTheme: Boolean,
        url: String,
        eventSink: (TimelineEvent) -> Unit,
        customTab: Boolean
    ) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                // Open the room member profile, it will fallback to
                // the user profile if the user is not in the room
                callback.navigateToRoomMemberDetails(permalink.userId)
            }
            is PermalinkData.RoomLink -> {
                handleRoomLinkClick(permalink, eventSink)
            }
            is PermalinkData.FallbackLink -> {
                if (customTab) {
                    activity.openUrlInChromeCustomTab(null, darkTheme, url)
                } else {
                    activity.openUrlInExternalApp(url)
                }
            }
            is PermalinkData.RoomEmailInviteLink -> {
                activity.openUrlInChromeCustomTab(null, darkTheme, url)
            }
        }
    }

    private fun handleRoomLinkClick(
        roomLink: PermalinkData.RoomLink,
        eventSink: (TimelineEvent) -> Unit,
    ) {
        if (room.matches(roomLink.roomIdOrAlias)) {
            val eventId = roomLink.eventId
            if (eventId != null) {
                eventSink(TimelineEvent.FocusOnEvent(eventId))
            } else {
                // Click on the same room, navigate up
                // Note that it can not be enough to go back to the room if the thread has been opened
                // following a permalink from another thread. In this case navigating up will go back
                // to the previous thread. But this should not happen often.
                navigateUp()
            }
        } else {
            callback.handlePermalinkClick(roomLink)
        }
    }

    override fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        callback.navigateToEventDebugInfo(eventId, debugInfo)
    }

    override fun forwardEvents(eventIds: List<EventId>) {
        callback.handleForwardEventClick(eventIds)
    }

    override fun exitTimelineSelection() {
        timelinePresenter?.exitSelectionMode()
    }

    override fun navigateToReportMessage(eventId: EventId, senderId: UserId) {
        callback.navigateToReportMessage(eventId, senderId)
    }

    override fun navigateToEditPoll(eventId: EventId) {
        callback.navigateToEditPoll(eventId)
    }

    override fun navigateToPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?) {
        callback.navigateToPreviewAttachments(attachments, inReplyToEventId)
    }

    override fun navigateToRoom(roomId: RoomId, eventId: EventId?, serverNames: List<String>) {
        val permalinkData = PermalinkData.RoomLink(roomId.toRoomIdOrAlias(), eventId, viaParameters = serverNames.toImmutableList())
        callback.handlePermalinkClick(permalinkData)
    }

    override fun navigateToMember(userId: UserId) {
        callback.navigateToRoomMemberDetails(userId)
    }

    override fun navigateToThread(threadRootId: ThreadId, focusedEventId: EventId?) {
        callback.navigateToThread(threadRootId, focusedEventId)
    }

    override fun navigateToDeveloperSettings() {
        callback.navigateToDeveloperSettings()
    }

    override fun navigateToCurrentLiveLocation() {
        // Shouldn't happen because LiveLocationSharingBanner is not shown in threads.
        callback.navigateToCurrentLiveLocation()
    }

    override fun close() = navigateUp()

    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()
        val canUseOverlay = !isTalkbackActive() && !hasExternalKeyboard()
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
        ) {
            // Only display the actual UI and lifecycle logic if the presenter is loaded
            presenter?.present()?.let { state ->
                OnLifecycleEvent { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> state.composerState.eventSink(MessageComposerEvent.SaveDraft)
                        else -> Unit
                    }
                }

                MessagesView(
                    state = state,
                    onBackClick = this::navigateUp,
                    onRoomDetailsClick = {},
                    onEventContentClick = { isLive, event ->
                        timelineController?.let { controller ->
                            if (isLive) {
                                callback.handleEventClick(controller.mainTimelineMode(), event, canUseOverlay)
                            } else {
                                val detachedTimelineMode = controller.detachedTimelineMode()
                                if (detachedTimelineMode != null) {
                                    callback.handleEventClick(detachedTimelineMode, event, canUseOverlay)
                                } else {
                                    false
                                }
                            }
                        } == true
                    },
                    onGalleryEventItemClick = { isLive, event, index ->
                        timelineController?.let { controller ->
                            if (isLive || controller.detachedTimelineMode() != null) {
                                callback.handleGalleryItemClick(event, index, canUseOverlay)
                            } else {
                                false
                            }
                        } == true
                    },
                    onUserDataClick = callback::navigateToRoomMemberDetails,
                    onLinkClick = { url, customTab ->
                        onLinkClick(
                            activity = activity,
                            darkTheme = isDark,
                            url = url,
                            eventSink = state.timelineState.eventSink,
                            customTab = customTab,
                        )
                    },
                    onSendLocationClick = callback::navigateToSendLocation,
                    onCreatePollClick = callback::navigateToCreatePoll,
                    onJoinCallClick = { isAudioCall ->
                        callback.navigateToRoomCall(room.roomId, isAudioCall)
                    },
                    onViewAllPinnedMessagesClick = {},
                    modifier = modifier,
                    knockRequestsBannerView = {},
                    customReactionBottomSheet = {
                        CustomReactionBottomSheet(
                            state = state.customReactionState,
                            onSelectEmoji = { uniqueId, emoji ->
                                state.eventSink(MessagesEvent.ToggleReaction(emoji.unicode, uniqueId))
                            },
                            emojiPickerRenderer = emojiPickerRenderer,
                        )
                    },
                    onThreadsListClick = {},
                )

                roomMemberModerationRenderer.Render(
                    state = state.roomMemberModerationState,
                    onSelectAction = { action, target ->
                        when (action) {
                            is ModerationAction.DisplayProfile -> callback.navigateToRoomMemberDetails(target.userId)
                            else -> state.roomMemberModerationState.eventSink(RoomMemberModerationEvent.ProcessAction(action, target))
                        }
                    },
                    onAvatarClick = { user ->
                        user.avatarUrl?.let { url ->
                            callback.navigateToAvatarPreview(user.getBestName(), url)
                        }
                    },
                    modifier = Modifier,
                )

                var focusedEventId by rememberSaveable {
                    mutableStateOf(inputs.focusedEventId)
                }
                LaunchedEffect(Unit) {
                    focusedEventId?.also { eventId ->
                        state.timelineState.eventSink(TimelineEvent.FocusOnEvent(eventId))
                    }
                    // Reset the focused event id to null to avoid refocusing when restoring node.
                    focusedEventId = null
                }
            }
        }
    }
}
