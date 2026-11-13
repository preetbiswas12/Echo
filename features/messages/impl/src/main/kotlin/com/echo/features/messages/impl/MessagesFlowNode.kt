/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.children.nodeOrNull
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.echo.annotations.ContributesNode
import com.echo.features.call.api.CallData
import com.echo.features.call.api.ElementCallEntryPoint
import com.echo.features.forward.api.ForwardEntryPoint
import com.echo.features.knockrequests.api.list.KnockRequestsListEntryPoint
import com.echo.features.location.api.LocationService
import com.echo.features.location.api.RenderingMapsNotSupportedDialog
import com.echo.features.location.api.ShareLocationEntryPoint
import com.echo.features.location.api.ShowLocationEntryPoint
import com.echo.features.location.api.ShowLocationMode
import com.echo.features.messages.api.MessagesEntryPoint
import com.echo.features.messages.impl.attachments.Attachment
import com.echo.features.messages.impl.attachments.preview.AttachmentsPreviewNode
import com.echo.features.messages.impl.pinned.DefaultPinnedEventsTimelineProvider
import com.echo.features.messages.impl.pinned.list.PinnedMessagesListNode
import com.echo.features.messages.impl.report.ReportMessageNode
import com.echo.features.messages.impl.threads.ThreadedMessagesNode
import com.echo.features.messages.impl.threads.list.ThreadsListNode
import com.echo.features.messages.impl.timeline.TimelineController
import com.echo.features.messages.impl.timeline.TimelineSelectionOwner
import com.echo.features.messages.impl.timeline.debug.EventDebugInfoNode
import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.features.messages.impl.timeline.model.event.GalleryItem
import com.echo.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemEventContentWithAttachment
import com.echo.features.messages.impl.timeline.model.event.TimelineItemFileContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemImageContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import com.echo.features.messages.impl.timeline.model.event.blurHash
import com.echo.features.messages.impl.timeline.model.event.duration
import com.echo.features.poll.api.create.CreatePollEntryPoint
import com.echo.features.poll.api.create.CreatePollMode
import com.echo.libraries.androidutils.system.DeviceHasVulkanSupport
import com.echo.libraries.architecture.BackstackView
import com.echo.libraries.architecture.BaseFlowNode
import com.echo.libraries.architecture.OverlayView
import com.echo.libraries.architecture.callback
import com.echo.libraries.architecture.createNode
import com.echo.libraries.architecture.overlay.Overlay
import com.echo.libraries.architecture.overlay.operation.hide
import com.echo.libraries.architecture.overlay.operation.show
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.dateformatter.api.DateFormatter
import com.echo.libraries.dateformatter.api.DateFormatterMode
import com.echo.libraries.dateformatter.api.toHumanReadableDuration
import com.echo.libraries.designsystem.components.async.AsyncIndicator
import com.echo.libraries.designsystem.components.async.AsyncIndicatorHost
import com.echo.libraries.designsystem.components.async.AsyncIndicatorState
import com.echo.libraries.di.RoomScope
import com.echo.libraries.htmlrenderer.api.LocalMentionDisplayNames
import com.echo.libraries.htmlrenderer.api.MentionDisplayNames
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.core.toRoomIdOrAlias
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.libraries.matrix.api.permalink.PermalinkData
import com.echo.libraries.matrix.api.room.BaseRoom
import com.echo.libraries.matrix.api.room.activeRoomMembers
import com.echo.libraries.matrix.api.room.alias.matches
import com.echo.libraries.matrix.api.roomlist.RoomListService
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import com.echo.libraries.matrix.ui.messages.LocalRoomMemberProfilesCache
import com.echo.libraries.matrix.ui.messages.RoomMemberProfilesCache
import com.echo.libraries.matrix.ui.messages.RoomNamesCache
import com.echo.libraries.mediaviewer.api.GalleryInfo
import com.echo.libraries.mediaviewer.api.GalleryItemData
import com.echo.libraries.mediaviewer.api.MediaInfo
import com.echo.libraries.mediaviewer.api.MediaViewerEntryPoint
import com.echo.libraries.textcomposer.mentions.LocalMentionSpanUpdater
import com.echo.libraries.textcomposer.mentions.MentionSpanTheme
import com.echo.libraries.textcomposer.mentions.MentionSpanUpdater
import com.echo.services.analytics.api.AnalyticsService
import com.echo.services.analyticsproviders.api.trackers.captureInteraction
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.Interaction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration.Companion.milliseconds

@ContributesNode(RoomScope::class)
@AssistedInject
class MessagesFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val roomListService: RoomListService,
    private val sessionId: SessionId,
    private val shareLocationEntryPoint: ShareLocationEntryPoint,
    private val showLocationEntryPoint: ShowLocationEntryPoint,
    private val createPollEntryPoint: CreatePollEntryPoint,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val mediaViewerEntryPoint: MediaViewerEntryPoint,
    private val forwardEntryPoint: ForwardEntryPoint,
    private val analyticsService: AnalyticsService,
    private val locationService: LocationService,
    private val room: BaseRoom,
    private val roomMemberProfilesCache: RoomMemberProfilesCache,
    private val roomNamesCache: RoomNamesCache,
    private val mentionSpanUpdater: MentionSpanUpdater,
    private val mentionSpanTheme: MentionSpanTheme,
    private val pinnedEventsTimelineProvider: DefaultPinnedEventsTimelineProvider,
    private val timelineController: TimelineController,
    private val knockRequestsListEntryPoint: KnockRequestsListEntryPoint,
    private val dateFormatter: DateFormatter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val hasVulkanSupport: DeviceHasVulkanSupport,
) : BaseFlowNode<MessagesFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = plugins.filterIsInstance<MessagesEntryPoint.Params>().first().initialTarget.toNavTarget(),
        savedStateMap = buildContext.savedStateMap,
    ),
    overlay = Overlay(
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
), MessagesEntryPoint.NodeProxy {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data class Messages(val focusedEventId: EventId?) : NavTarget

        @Parcelize
        data class MediaViewer(
            val mode: MediaViewerEntryPoint.MediaViewerMode,
            val eventId: EventId?,
            val mediaInfo: MediaInfo,
            val mediaSource: MediaSource,
            val thumbnailSource: MediaSource?,
            val canUseOverlay: Boolean,
            val blurHash: String?,
        ) : NavTarget

        @Parcelize
        data class GalleryViewer(
            val eventId: EventId?,
            val galleryInfo: GalleryInfo,
            val canUseOverlay: Boolean,
            val galleryItems: List<GalleryItemData> = emptyList(),
        ) : NavTarget

        @Parcelize
        data class AttachmentPreview(val timelineMode: Timeline.Mode, val attachments: ImmutableList<Attachment>, val inReplyToEventId: EventId?) : NavTarget

        @Parcelize
        data class LocationViewer(val mode: ShowLocationMode) : NavTarget

        @Parcelize
        data class EventDebugInfo(val eventId: EventId?, val debugInfo: TimelineItemDebugInfo) : NavTarget

        @Parcelize
        data class ForwardEvent(val eventIds: List<EventId>) : NavTarget

        @Parcelize
        data class ReportMessage(val eventId: EventId, val senderId: UserId) : NavTarget

        @Parcelize
        data class SendLocation(val timelineMode: Timeline.Mode) : NavTarget

        @Parcelize
        data class CreatePoll(val timelineMode: Timeline.Mode) : NavTarget

        @Parcelize
        data class EditPoll(val timelineMode: Timeline.Mode, val eventId: EventId) : NavTarget

        @Parcelize
        data object PinnedMessagesList : NavTarget

        @Parcelize
        data object KnockRequestsList : NavTarget

        @Parcelize
        data class Thread(val threadRootId: ThreadId, val focusedEventId: EventId?) : NavTarget

        @Parcelize
        data object ThreadsList : NavTarget

        @Parcelize
        data class AvatarPreview(val name: String, val avatarUrl: String) : NavTarget
    }

    private val callback: MessagesEntryPoint.Callback = callback()
    private val asyncIndicatorState = AsyncIndicatorState()

    private var displayVulkanNotSupportedError by mutableStateOf(false)

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onDestroy = {
                timelineController.close()
            }
        )
        setupCacheUpdaters()

        pinnedEventsTimelineProvider.launchIn(lifecycleScope)
    }

    private fun setupCacheUpdaters() {
        room.membersStateFlow
            .onEach { membersState ->
                withContext(coroutineDispatchers.computation) {
                    roomMemberProfilesCache.replace(membersState.activeRoomMembers())
                }
            }
            .launchIn(lifecycleScope)

        roomListService
            .allRooms
            .summaries
            .onEach {
                withContext(coroutineDispatchers.computation) {
                    roomNamesCache.replace(it)
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Messages -> {
                val callback = object : MessagesNode.Callback {
                    override fun navigateToRoomDetails() {
                        callback.navigateToRoomDetails()
                    }

                    override fun handleEventClick(
                        timelineMode: Timeline.Mode,
                        event: TimelineItem.Event,
                        canUseOverlay: Boolean,
                    ): Boolean {
                        return processEventClick(
                            timelineMode = timelineMode,
                            event = event,
                            canUseOverlay = canUseOverlay,
                        )
                    }

                    override fun handleGalleryItemClick(
                        event: TimelineItem.Event,
                        galleryItemIndex: Int,
                        canUseOverlay: Boolean,
                    ): Boolean {
                        return processGalleryEventClick(
                            event = event,
                            canUseOverlay = canUseOverlay,
                            galleryItemIndex = galleryItemIndex,
                        )
                    }

                    override fun navigateToPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?) {
                        backstack.push(
                            NavTarget.AttachmentPreview(
                                attachments = attachments,
                                timelineMode = Timeline.Mode.Live,
                                inReplyToEventId = inReplyToEventId,
                            )
                        )
                    }

                    override fun navigateToRoomMemberDetails(userId: UserId) {
                        callback.navigateToRoomMemberDetails(userId)
                    }

                    override fun handlePermalinkClick(data: PermalinkData) {
                        callback.handlePermalinkClick(data, pushToBackstack = true)
                    }

                    override fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
                        backstack.push(NavTarget.EventDebugInfo(eventId, debugInfo))
                    }

                    override fun forwardEvents(eventIds: List<EventId>) {
                        backstack.push(NavTarget.ForwardEvent(eventIds))
                    }

                    override fun navigateToReportMessage(eventId: EventId, senderId: UserId) {
                        backstack.push(NavTarget.ReportMessage(eventId, senderId))
                    }

                    override fun navigateToSendLocation() {
                        if (hasVulkanSupport()) {
                            backstack.push(NavTarget.SendLocation(Timeline.Mode.Live))
                        } else {
                            displayVulkanNotSupportedError = true
                        }
                    }

                    override fun navigateToCreatePoll() {
                        backstack.push(NavTarget.CreatePoll(Timeline.Mode.Live))
                    }

                    override fun navigateToEditPoll(eventId: EventId) {
                        backstack.push(NavTarget.EditPoll(Timeline.Mode.Live, eventId))
                    }

                    override fun navigateToCurrentLiveLocation() {
                        if (hasVulkanSupport()) {
                            backstack.push(NavTarget.LocationViewer(ShowLocationMode.Live(senderId = sessionId)))
                        } else {
                            displayVulkanNotSupportedError = true
                        }
                    }

                    override fun navigateToRoomCall(roomId: RoomId, isAudioCall: Boolean) {
                        val callData = CallData(
                            sessionId = sessionId,
                            roomId = roomId,
                            isAudioCall = isAudioCall,
                        )
                        analyticsService.captureInteraction(Interaction.Name.MobileRoomCallButton)
                        elementCallEntryPoint.startCall(callData)
                    }

                    override fun navigateToPinnedMessagesList() {
                        backstack.push(NavTarget.PinnedMessagesList)
                    }

                    override fun navigateToKnockRequestsList() {
                        backstack.push(NavTarget.KnockRequestsList)
                    }

                    override fun navigateToThread(threadRootId: ThreadId, focusedEventId: EventId?) {
                        backstack.push(NavTarget.Thread(threadRootId, focusedEventId))
                    }

                    override fun navigateToThreadsList() {
                        backstack.push(NavTarget.ThreadsList)
                    }

                    override fun navigateToDeveloperSettings() {
                        callback.navigateToDeveloperSettings()
                    }

                    override fun navigateToAvatarPreview(username: String, avatarUrl: String) {
                        overlay.show(NavTarget.AvatarPreview(username, avatarUrl))
                    }
                }
                val inputs = MessagesNode.Inputs(focusedEventId = navTarget.focusedEventId)
                createNode<MessagesNode>(buildContext, listOf(callback, inputs))
            }
            is NavTarget.MediaViewer -> {
                val params = MediaViewerEntryPoint.Params.RoomMedia(
                    mode = navTarget.mode,
                    eventId = navTarget.eventId,
                    mediaInfo = navTarget.mediaInfo,
                    mediaSource = navTarget.mediaSource,
                    thumbnailSource = navTarget.thumbnailSource,
                    blurHash = navTarget.blurHash,
                )
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        if (navTarget.canUseOverlay) {
                            overlay.hide()
                        } else {
                            backstack.pop()
                        }
                    }

                    override fun viewInTimeline(eventId: EventId) {
                        this@MessagesFlowNode.viewInTimeline(eventId)
                    }

                    override fun forwardEvent(eventId: EventId) {
                        // Need to go to the parent because of the overlay
                        callback.forwardEvent(eventId)
                    }
                }
                mediaViewerEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback
                )
            }
            is NavTarget.GalleryViewer -> {
                val params = MediaViewerEntryPoint.Params.EventGallery(
                    eventId = navTarget.eventId,
                    galleryInfo = navTarget.galleryInfo,
                    galleryItems = navTarget.galleryItems,
                )
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        if (navTarget.canUseOverlay) {
                            overlay.hide()
                        } else {
                            backstack.pop()
                        }
                    }

                    override fun viewInTimeline(eventId: EventId) {
                        this@MessagesFlowNode.viewInTimeline(eventId)
                    }

                    override fun forwardEvent(eventId: EventId) {
                        // Need to go to the parent because of the overlay
                        callback.forwardEvent(eventId)
                    }
                }
                mediaViewerEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback
                )
            }
            is NavTarget.AttachmentPreview -> {
                val inputs = AttachmentsPreviewNode.Inputs(
                    attachments = navTarget.attachments,
                    timelineMode = navTarget.timelineMode,
                    inReplyToEventId = navTarget.inReplyToEventId,
                )
                createNode<AttachmentsPreviewNode>(buildContext, listOf(inputs))
            }
            is NavTarget.LocationViewer -> {
                val inputs = ShowLocationEntryPoint.Inputs(navTarget.mode)
                showLocationEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    inputs = inputs,
                )
            }
            is NavTarget.EventDebugInfo -> {
                val inputs = EventDebugInfoNode.Inputs(navTarget.eventId, navTarget.debugInfo)
                createNode<EventDebugInfoNode>(buildContext, listOf(inputs))
            }
            is NavTarget.ForwardEvent -> {
                val params = ForwardEntryPoint.Params(
                    eventIds = navTarget.eventIds,
                    fromRoomId = room.roomId,
                )
                val callback = object : ForwardEntryPoint.Callback {
                    override fun onDone(roomIds: List<RoomId>) {
                        // Exit selection mode only if forwarding actually happened.
                        if (roomIds.isNotEmpty()) {
                            exitSelectionMode()
                        }
                        backstack.pop()
                        val singleRoomId = roomIds.singleOrNull()
                        when {
                            singleRoomId != null -> callback.navigateToRoom(singleRoomId)
                            roomIds.isNotEmpty() -> {
                                val numberOfMessages = navTarget.eventIds.size
                                asyncIndicatorState.enqueue(durationMs = AsyncIndicator.DURATION_SHORT) {
                                    AsyncIndicator.Custom(
                                        text = pluralStringResource(R.plurals.screen_room_messages_forwarded, numberOfMessages, numberOfMessages),
                                    )
                                }
                            }
                        }
                    }
                }
                forwardEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback,
                )
            }
            is NavTarget.ReportMessage -> {
                val inputs = ReportMessageNode.Inputs(navTarget.eventId, navTarget.senderId)
                createNode<ReportMessageNode>(buildContext, listOf(inputs))
            }
            is NavTarget.SendLocation -> {
                shareLocationEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    timelineMode = navTarget.timelineMode,
                )
            }
            is NavTarget.CreatePoll -> {
                createPollEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = CreatePollEntryPoint.Params(
                        timelineMode = navTarget.timelineMode,
                        mode = CreatePollMode.NewPoll
                    ),
                )
            }
            is NavTarget.EditPoll -> {
                createPollEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = CreatePollEntryPoint.Params(
                        timelineMode = navTarget.timelineMode,
                        mode = CreatePollMode.EditPoll(eventId = navTarget.eventId)
                    ),
                )
            }
            NavTarget.PinnedMessagesList -> {
                val callback = object : PinnedMessagesListNode.Callback {
                    override fun handleEventClick(event: TimelineItem.Event, canUseOverlay: Boolean) {
                        processEventClick(
                            timelineMode = Timeline.Mode.PinnedEvents,
                            event = event,
                            canUseOverlay = canUseOverlay,
                        )
                    }

                    override fun handleGalleryItemClick(
                        event: TimelineItem.Event,
                        galleryItemIndex: Int,
                        canUseOverlay: Boolean,
                    ) {
                        processGalleryEventClick(
                            event = event,
                            galleryItemIndex = galleryItemIndex,
                            canUseOverlay = canUseOverlay,
                        )
                    }

                    override fun navigateToRoomMemberDetails(userId: UserId) {
                        callback.navigateToRoomMemberDetails(userId)
                    }

                    override fun viewInTimeline(eventId: EventId) {
                        this@MessagesFlowNode.viewInTimeline(eventId)
                    }

                    override fun handlePermalinkClick(data: PermalinkData.RoomLink) {
                        callback.handlePermalinkClick(data, pushToBackstack = !room.matches(data.roomIdOrAlias))
                    }

                    override fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
                        backstack.push(NavTarget.EventDebugInfo(eventId, debugInfo))
                    }

                    override fun handleForwardEventClick(eventId: EventId) {
                        backstack.push(NavTarget.ForwardEvent(eventIds = listOf(eventId)))
                    }

                    override fun navigateToThread(threadRootId: ThreadId) {
                        backstack.push(NavTarget.Thread(threadRootId, null))
                    }
                }
                createNode<PinnedMessagesListNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.KnockRequestsList -> {
                knockRequestsListEntryPoint.createNode(this, buildContext)
            }
            is NavTarget.Thread -> {
                val inputs = ThreadedMessagesNode.Inputs(
                    threadRootEventId = navTarget.threadRootId,
                    focusedEventId = navTarget.focusedEventId,
                )
                val callback = object : ThreadedMessagesNode.Callback {
                    override fun handleEventClick(
                        timelineMode: Timeline.Mode,
                        event: TimelineItem.Event,
                        canUseOverlay: Boolean,
                    ): Boolean {
                        return processEventClick(
                            timelineMode = timelineMode,
                            event = event,
                            canUseOverlay = canUseOverlay,
                        )
                    }

                    override fun handleGalleryItemClick(
                        event: TimelineItem.Event,
                        galleryItemIndex: Int,
                        canUseOverlay: Boolean,
                    ): Boolean {
                        return processGalleryEventClick(
                            event = event,
                            canUseOverlay = canUseOverlay,
                            galleryItemIndex = galleryItemIndex,
                        )
                    }

                    override fun navigateToPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?) {
                        backstack.push(
                            NavTarget.AttachmentPreview(
                                attachments = attachments,
                                timelineMode = Timeline.Mode.Thread(navTarget.threadRootId),
                                inReplyToEventId = inReplyToEventId,
                            )
                        )
                    }

                    override fun navigateToRoomMemberDetails(userId: UserId) {
                        callback.navigateToRoomMemberDetails(userId)
                    }

                    override fun handlePermalinkClick(data: PermalinkData) {
                        callback.handlePermalinkClick(data, pushToBackstack = true)
                    }

                    override fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
                        backstack.push(NavTarget.EventDebugInfo(eventId, debugInfo))
                    }

                    override fun handleForwardEventClick(eventIds: List<EventId>) {
                        backstack.push(NavTarget.ForwardEvent(eventIds))
                    }

                    override fun navigateToReportMessage(eventId: EventId, senderId: UserId) {
                        backstack.push(NavTarget.ReportMessage(eventId, senderId))
                    }

                    override fun navigateToSendLocation() {
                        if (hasVulkanSupport()) {
                            backstack.push(NavTarget.SendLocation(Timeline.Mode.Thread(navTarget.threadRootId)))
                        } else {
                            displayVulkanNotSupportedError = true
                        }
                    }

                    override fun navigateToCreatePoll() {
                        backstack.push(NavTarget.CreatePoll(Timeline.Mode.Thread(navTarget.threadRootId)))
                    }

                    override fun navigateToEditPoll(eventId: EventId) {
                        backstack.push(NavTarget.EditPoll(Timeline.Mode.Thread(navTarget.threadRootId), eventId))
                    }

                    override fun navigateToCurrentLiveLocation() {
                        if (hasVulkanSupport()) {
                            backstack.push(NavTarget.LocationViewer(ShowLocationMode.Live(senderId = sessionId)))
                        } else {
                            displayVulkanNotSupportedError = true
                        }
                    }

                    override fun navigateToRoomCall(roomId: RoomId, isAudioCall: Boolean) {
                        val callData = CallData(
                            sessionId = sessionId,
                            roomId = roomId,
                            isAudioCall = isAudioCall
                        )
                        analyticsService.captureInteraction(Interaction.Name.MobileRoomCallButton)
                        elementCallEntryPoint.startCall(callData)
                    }

                    override fun navigateToThread(threadRootId: ThreadId, focusedEventId: EventId?) {
                        backstack.push(NavTarget.Thread(threadRootId, focusedEventId))
                    }

                    override fun navigateToDeveloperSettings() {
                        callback.navigateToDeveloperSettings()
                    }

                    override fun navigateToAvatarPreview(username: String, avatarUrl: String) {
                        overlay.show(NavTarget.AvatarPreview(username, avatarUrl))
                    }
                }
                createNode<ThreadedMessagesNode>(buildContext, listOf(inputs, callback))
            }
            NavTarget.ThreadsList -> {
                val callback = object : ThreadsListNode.Callback {
                    override fun openThread(threadId: ThreadId) {
                        backstack.push(NavTarget.Thread(threadId, focusedEventId = null))
                    }
                }
                createNode<ThreadsListNode>(buildContext, listOf(callback))
            }
            is NavTarget.AvatarPreview -> {
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        overlay.hide()
                    }

                    override fun viewInTimeline(eventId: EventId) {
                        // Cannot happen
                    }

                    override fun forwardEvent(eventId: EventId) {
                        // Cannot happen
                    }
                }
                val params = mediaViewerEntryPoint.createParamsForAvatar(
                    filename = navTarget.name,
                    avatarUrl = navTarget.avatarUrl,
                )
                mediaViewerEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback,
                )
            }
        }
    }

    private fun viewInTimeline(eventId: EventId) {
        val permalinkData = PermalinkData.RoomLink(
            roomIdOrAlias = room.roomId.toRoomIdOrAlias(),
            eventId = eventId,
        )
        callback.handlePermalinkClick(permalinkData, pushToBackstack = false)
    }

    private fun processEventClick(
        timelineMode: Timeline.Mode,
        event: TimelineItem.Event,
        canUseOverlay: Boolean,
    ): Boolean {
        val navTarget = when (event.content) {
            is TimelineItemImageContent -> {
                buildMediaViewerNavTarget(
                    mode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(timelineMode),
                    event = event,
                    content = event.content,
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                    canUseOverlay = canUseOverlay,
                )
            }
            is TimelineItemVideoContent -> {
                buildMediaViewerNavTarget(
                    mode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(timelineMode),
                    event = event,
                    content = event.content,
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                    canUseOverlay = canUseOverlay,
                )
            }
            is TimelineItemFileContent -> {
                buildMediaViewerNavTarget(
                    mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode),
                    event = event,
                    content = event.content,
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                    canUseOverlay = canUseOverlay,
                )
            }
            is TimelineItemAudioContent -> {
                buildMediaViewerNavTarget(
                    mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode),
                    event = event,
                    content = event.content,
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = null,
                    canUseOverlay = canUseOverlay,
                )
            }
            is TimelineItemLocationContent -> {
                if (hasVulkanSupport()) {
                    val mode = when (event.content.mode) {
                        is TimelineItemLocationContent.Mode.Live -> ShowLocationMode.Live(event.senderId)
                        is TimelineItemLocationContent.Mode.Static -> ShowLocationMode.Static(
                            location = event.content.mode.location,
                            senderName = event.safeSenderName,
                            senderId = event.senderId,
                            senderAvatarUrl = event.senderAvatar.url,
                            timestamp = event.sentTimeMillis,
                            assetType = event.content.assetType,
                        )
                    }
                    NavTarget.LocationViewer(mode = mode).takeIf { locationService.isServiceAvailable().value }
                } else {
                    displayVulkanNotSupportedError = true
                    null
                }
            }
            else -> null
        }
        return when (navTarget) {
            is NavTarget.MediaViewer -> {
                if (canUseOverlay) {
                    overlay.show(navTarget)
                } else {
                    backstack.push(navTarget)
                }
                true
            }
            is NavTarget.LocationViewer -> {
                backstack.push(navTarget)
                true
            }
            else -> false
        }
    }

    private fun processGalleryEventClick(
        event: TimelineItem.Event,
        galleryItemIndex: Int,
        canUseOverlay: Boolean,
    ): Boolean {
        val navTarget = when (event.content) {
            is TimelineItemGalleryContent -> {
                val galleryInfo = GalleryInfo(
                    caption = event.content.caption,
                    htmlCaption = event.content.htmlCaption,
                    senderId = event.senderId,
                    senderName = event.safeSenderName,
                    senderAvatar = event.senderAvatar.url,
                    dateSent = dateFormatter.format(
                        event.sentTimeMillis,
                        mode = DateFormatterMode.Day,
                    ),
                    dateSentFull = dateFormatter.format(
                        timestamp = event.sentTimeMillis,
                        mode = DateFormatterMode.Full,
                    ),
                    initialIndex = galleryItemIndex,
                )
                val galleryItems = event.content.items.map { galleryItem ->
                    GalleryItemData(
                        filename = galleryItem.filename,
                        mimeType = galleryItem.mimeType,
                        mediaSource = galleryItem.mediaSource,
                        thumbnailSource = galleryItem.thumbnailSource,
                        type = galleryItem.type.toMediaViewerType(),
                        blurHash = galleryItem.blurhash,
                    )
                }.reversed()
                NavTarget.GalleryViewer(
                    eventId = event.eventId,
                    galleryInfo = galleryInfo,
                    canUseOverlay = canUseOverlay,
                    galleryItems = galleryItems,
                )
            }
            is TimelineItemAttachmentsContent -> {
                val galleryInfo = GalleryInfo(
                    caption = event.content.caption,
                    htmlCaption = event.content.htmlCaption,
                    senderId = event.senderId,
                    senderName = event.safeSenderName,
                    senderAvatar = event.senderAvatar.url,
                    dateSent = dateFormatter.format(
                        event.sentTimeMillis,
                        mode = DateFormatterMode.Day,
                    ),
                    dateSentFull = dateFormatter.format(
                        timestamp = event.sentTimeMillis,
                        mode = DateFormatterMode.Full,
                    ),
                    initialIndex = galleryItemIndex,
                )
                val galleryItems = event.content.attachments.map { attachment ->
                    GalleryItemData(
                        filename = attachment.filename,
                        mimeType = attachment.mimeType,
                        mediaSource = attachment.mediaSource,
                        thumbnailSource = attachment.thumbnailSource,
                        type = GalleryItemData.Type.File,
                        blurHash = null,
                    )
                }.reversed()
                NavTarget.GalleryViewer(
                    eventId = event.eventId,
                    galleryInfo = galleryInfo,
                    canUseOverlay = canUseOverlay,
                    galleryItems = galleryItems,
                )
            }
            else -> null
        }
        if (navTarget != null) {
            if (canUseOverlay) {
                overlay.show(navTarget)
            } else {
                backstack.push(navTarget)
            }
            return true
        }
        return false
    }

    private fun buildMediaViewerNavTarget(
        mode: MediaViewerEntryPoint.MediaViewerMode,
        event: TimelineItem.Event,
        content: TimelineItemEventContentWithAttachment,
        mediaSource: MediaSource,
        thumbnailSource: MediaSource?,
        canUseOverlay: Boolean,
    ): NavTarget {
        return NavTarget.MediaViewer(
            mode = mode,
            eventId = event.eventId,
            mediaInfo = MediaInfo(
                filename = content.filename,
                fileSize = content.fileSize,
                caption = content.caption,
                htmlCaption = content.htmlCaption,
                mimeType = content.mimeType,
                formattedFileSize = content.formattedFileSize,
                fileExtension = content.fileExtension,
                senderId = event.senderId,
                senderName = event.safeSenderName,
                senderAvatar = event.senderAvatar.url,
                dateSent = dateFormatter.format(
                    event.sentTimeMillis,
                    mode = DateFormatterMode.Day,
                ),
                dateSentFull = dateFormatter.format(
                    timestamp = event.sentTimeMillis,
                    mode = DateFormatterMode.Full,
                ),
                waveform = (content as? TimelineItemVoiceContent)?.waveform,
                duration = content.duration()?.toHumanReadableDuration(),
            ),
            mediaSource = mediaSource,
            thumbnailSource = thumbnailSource,
            canUseOverlay = canUseOverlay,
            blurHash = content.blurHash(),
        )
    }

    override suspend fun attachThread(threadId: ThreadId, focusedEventId: EventId?) {
        // Wait until we have the UI for the main timeline attached
        waitForChildAttached<MessagesNode>()
        // Give some time for the items in the main timeline to be received, otherwise loading the focused thread root id won't work
        // (look at TimelineItemIndexer and firstProcessLatch for more info)
        delay(10.milliseconds)
        // Then push the new threads screen on top
        backstack.push(NavTarget.Thread(threadId, focusedEventId))
    }

    private fun exitSelectionMode() {
        val prevKey = backstack.elements.value
            .lastOrNull { it.targetState == BackStack.State.STASHED }
            ?.key
        val timelineSelectionOwner = children.value[prevKey]?.nodeOrNull as? TimelineSelectionOwner
        timelineSelectionOwner?.exitTimelineSelection()
    }

    @Composable
    override fun View(modifier: Modifier) {
        mentionSpanTheme.updateStyles()

        if (displayVulkanNotSupportedError) {
            RenderingMapsNotSupportedDialog { displayVulkanNotSupportedError = false }
        }

        // The State instances are passed as is, so the provided value stays the same when the display names change
        val userDisplayNames = roomMemberProfilesCache.displayNames.collectAsState()
        val roomDisplayNames = roomNamesCache.displayNames.collectAsState()
        val userStatuses = roomMemberProfilesCache.displayedStatusEmojis.collectAsState()
        val mentionDisplayNames = remember(userDisplayNames, roomDisplayNames, userStatuses) {
            MentionDisplayNames(users = userDisplayNames, rooms = roomDisplayNames, userStatuses = userStatuses)
        }

        CompositionLocalProvider(
            LocalMentionSpanUpdater provides mentionSpanUpdater,
            LocalMentionDisplayNames provides mentionDisplayNames,
            LocalRoomMemberProfilesCache provides roomMemberProfilesCache,
        ) {
            Box(modifier = modifier) {
                BackstackView()
                OverlayView()
                AsyncIndicatorHost(modifier = Modifier.statusBarsPadding(), state = asyncIndicatorState)
            }
        }
    }
}

private fun GalleryItem.Type.toMediaViewerType(): GalleryItemData.Type {
    return when (this) {
        GalleryItem.Type.Image -> GalleryItemData.Type.Image
        GalleryItem.Type.Video -> GalleryItemData.Type.Video
        GalleryItem.Type.Audio -> GalleryItemData.Type.Audio
        GalleryItem.Type.File -> GalleryItemData.Type.File
    }
}
