/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintLayout
import com.echo.compound.theme.ElementTheme
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.messages.impl.timeline.TimelineEvent
import com.echo.features.messages.impl.timeline.TimelineRoomInfo
import com.echo.features.messages.impl.timeline.aTimelineItemEvent
import com.echo.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import com.echo.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import com.echo.features.messages.impl.timeline.components.receipt.TimelineItemReadReceiptView
import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.features.messages.impl.timeline.model.TimelineItemGroupPosition
import com.echo.features.messages.impl.timeline.model.TimelineItemReactions
import com.echo.features.messages.impl.timeline.model.TimelineItemSelectionData
import com.echo.features.messages.impl.timeline.model.TimelineItemThreadInfo
import com.echo.features.messages.impl.timeline.model.bubble.BubbleState
import com.echo.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemImageContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemPollContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemTextContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import com.echo.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import com.echo.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import com.echo.features.messages.impl.timeline.model.event.ensureActiveLiveLocation
import com.echo.features.messages.impl.timeline.protection.TimelineProtectionState
import com.echo.features.messages.impl.timeline.protection.mustBeProtected
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.designsystem.colors.AvatarColorsProvider
import com.echo.libraries.designsystem.components.EqualWidthColumn
import com.echo.libraries.designsystem.components.avatar.Avatar
import com.echo.libraries.designsystem.components.avatar.AvatarData
import com.echo.libraries.designsystem.components.avatar.AvatarSize
import com.echo.libraries.designsystem.components.avatar.AvatarType
import com.echo.libraries.designsystem.modifiers.niceClickable
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewWithExtraLargeHeight
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.preview.USER_NAME_ALICE
import com.echo.libraries.designsystem.swipe.SwipeableActionsState
import com.echo.libraries.designsystem.swipe.rememberSwipeableActionsState
import com.echo.libraries.designsystem.text.toPx
import com.echo.libraries.designsystem.theme.components.Icon
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.core.toThreadId
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.api.timeline.item.EmbeddedEventInfo
import com.echo.libraries.matrix.api.timeline.item.ThreadSummary
import com.echo.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import com.echo.libraries.matrix.api.timeline.item.event.MessageContent
import com.echo.libraries.matrix.api.timeline.item.event.MessageShield
import com.echo.libraries.matrix.api.timeline.item.event.ProfileDetails
import com.echo.libraries.matrix.api.timeline.item.event.TextMessageType
import com.echo.libraries.matrix.api.timeline.item.event.getAvatarUrl
import com.echo.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import com.echo.libraries.matrix.api.timeline.item.event.getDisplayName
import com.echo.libraries.matrix.api.timeline.item.event.mediaSources
import com.echo.libraries.matrix.api.timeline.item.event.toMatrixUser
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.matrix.ui.media.contentvalidation.collectOverallState
import com.echo.libraries.matrix.ui.media.contentvalidation.rememberEventContentValidationState
import com.echo.libraries.matrix.ui.messages.reply.InReplyToDetails
import com.echo.libraries.matrix.ui.messages.reply.InReplyToView
import com.echo.libraries.matrix.ui.messages.reply.content
import com.echo.libraries.matrix.ui.messages.reply.eventId
import com.echo.libraries.matrix.ui.messages.sender.SenderName
import com.echo.libraries.matrix.ui.messages.sender.SenderNameMode
import com.echo.libraries.testtags.TestTags
import com.echo.libraries.testtags.testTag
import com.echo.libraries.ui.common.layout.ContentAvoidingLayout
import com.echo.libraries.ui.common.layout.ContentAvoidingLayoutData
import com.echo.libraries.ui.strings.CommonPlurals
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.libraries.ui.utils.a11y.isTalkbackActive
import com.echo.libraries.ui.utils.text.detect
import io.element.android.wysiwyg.link.Link
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// The bubble has a negative margin to be placed a bit upper regarding the sender
// information and overlap the avatar.
val NEGATIVE_MARGIN_FOR_BUBBLE = (-8).dp

// Width of the transparent border around the sender avatar
val SENDER_AVATAR_BORDER_WIDTH = 3.dp

private val BUBBLE_INCOMING_OFFSET = 16.dp

/** Shared instance so that rows which never slide keep comparing equal, and keep skipping. */
internal val NoSelectionOffset: Density.() -> IntOffset = { IntOffset.Zero }

@Composable
fun TimelineItemEventRow(
    event: TimelineItem.Event,
    timelineMode: Timeline.Mode,
    timelineRoomInfo: TimelineRoomInfo,
    timelineProtectionState: TimelineProtectionState,
    isLastOutgoingMessage: Boolean,
    displayThreadSummaries: Boolean,
    onEventClick: () -> Unit,
    onGalleryItemClick: ((Int) -> Unit),
    onLongClick: () -> Unit,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    onUserDataClick: (MatrixUser) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (emoji: String, eventId: TimelineItem.Event) -> Unit,
    onReactionLongClick: (emoji: String, eventId: TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (eventId: TimelineItem.Event) -> Unit,
    onReadReceiptClick: (event: TimelineItem.Event) -> Unit,
    onSwipeToReply: () -> Unit,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectionData: TimelineItemSelectionData = TimelineItemSelectionData(),
    eventContentView: @Composable (Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit = { contentModifier, onContentLayoutChange ->
        // Only pass down a custom clickable lambda if the content can be clicked separately
        val onContentClick = onEventClick.takeUnless { event.isWholeContentClickable }

        TimelineItemEventContentView(
            eventId = event.eventId,
            content = event.content,
            timelineProtectionState = timelineProtectionState,
            onContentClick = onContentClick,
            onGalleryItemClick = onGalleryItemClick,
            onLongClick = onLongClick,
            onLinkClick = onLinkClick,
            onLinkLongClick = onLinkLongClick,
            eventSink = eventSink,
            modifier = contentModifier,
            onContentLayoutChange = onContentLayoutChange,
        )
    },
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    // While selecting, the row acts as a selection toggle rather than opening the context menu.
    val isSelectionToggle = selectionData.isSelectionModeActive && selectionData.canBeSelected
    // Selection mode opens a column of [SELECTION_COLUMN_WIDTH] at the start edge for the tick. Both offsets below are derived from this single
    // slide and resolved in the layout phase, so an animation frame moves them together without recomposing the row, and they cannot disagree.
    val selectionProgress = selectionData.progress
    val selectionSlide: Density.() -> Int = { (SELECTION_COLUMN_WIDTH * selectionProgress()).roundToPx() }
    // Incoming bubbles slide aside to open that column. Outgoing (own) ones are end-aligned, opposite the tick, so they already have room.
    val selectionContentOffset: Density.() -> IntOffset = if (event.isMine) {
        NoSelectionOffset
    } else {
        { IntOffset(x = selectionSlide(), y = 0) }
    }
    // The tick is a child of the layout the offset above slides, so it cancels that out to be positioned against the row, then sits one column
    // width before its resting place until the slide is complete. For an incoming row the two terms cancel and it simply rides the content in.
    val selectionIndicatorOffset: Density.() -> IntOffset = {
        IntOffset(x = selectionSlide() - SELECTION_COLUMN_WIDTH.roundToPx() - selectionContentOffset().x, y = 0)
    }

    val onContentClick = if (event.mustBeProtected()) {
        // In this case, let the content handle the click
        {}
    } else {
        onEventClick
    }

    fun onUserDataClick() {
        val sender = event.senderProfile.toMatrixUser(event.senderId)
        onUserDataClick(sender)
    }

    fun inReplyToClick() {
        val inReplyToEventId = event.inReplyTo?.eventId() ?: return
        inReplyToClick(inReplyToEventId)
    }

    val canReply = timelineRoomInfo.userHasPermissionToSendMessage && event.canBeRepliedTo
    val accessibilityActions = rememberTimelineItemAccessibilityActions(
        isEnabled = !isSelectionToggle,
        canReply = canReply,
        onLongClick = onLongClick,
        onSwipeToReply = onSwipeToReply,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { customActions = accessibilityActions }
    ) {
        if (event.groupPosition.isNew()) {
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }
        if (canReply) {
            val state: SwipeableActionsState = rememberSwipeableActionsState()
            val offset = state.offset.floatValue
            val swipeThresholdPx = 40.dp.toPx()
            val thresholdCrossed = abs(offset) > swipeThresholdPx
            SwipeSensitivity(3f) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .offset(selectionContentOffset)
                ) {
                    Row(modifier = Modifier.matchParentSize()) {
                        ReplySwipeIndicator({ offset / 120 })
                    }
                    TimelineItemEventRowContent(
                        selectionData = selectionData,
                        selectionIndicatorOffset = selectionIndicatorOffset,
                        event = event,
                        timelineMode = timelineMode,
                        timelineProtectionState = timelineProtectionState,
                        timelineRoomInfo = timelineRoomInfo,
                        interactionSource = interactionSource,
                        onContentClick = onContentClick,
                        onLongClick = onLongClick,
                        inReplyToClick = ::inReplyToClick,
                        onUserDataClick = ::onUserDataClick,
                        onReactionClick = { emoji -> onReactionClick(emoji, event) },
                        onReactionLongClick = { emoji -> onReactionLongClick(emoji, event) },
                        onMoreReactionsClick = { onMoreReactionsClick(event) },
                        modifier = Modifier
                            .absoluteOffset { IntOffset(x = offset.roundToInt(), y = 0) }
                            .draggable(
                                orientation = Orientation.Horizontal,
                                enabled = !state.isResettingOnRelease && !isSelectionToggle,
                                onDragStopped = {
                                    coroutineScope.launch {
                                        if (thresholdCrossed) {
                                            onSwipeToReply()
                                        }
                                        state.resetOffset()
                                    }
                                },
                                state = state.draggableState,
                            ),
                        eventSink = eventSink,
                        eventContentView = eventContentView,
                    )
                }
            }
        } else {
            TimelineItemEventRowContent(
                modifier = Modifier.offset(selectionContentOffset),
                selectionData = selectionData,
                selectionIndicatorOffset = selectionIndicatorOffset,
                event = event,
                timelineMode = timelineMode,
                timelineProtectionState = timelineProtectionState,
                timelineRoomInfo = timelineRoomInfo,
                interactionSource = interactionSource,
                onContentClick = onContentClick,
                onLongClick = onLongClick,
                inReplyToClick = ::inReplyToClick,
                onUserDataClick = ::onUserDataClick,
                onReactionClick = { emoji -> onReactionClick(emoji, event) },
                onReactionLongClick = { emoji -> onReactionLongClick(emoji, event) },
                onMoreReactionsClick = { onMoreReactionsClick(event) },
                eventSink = eventSink,
                eventContentView = eventContentView,
            )
        }

        if (displayThreadSummaries && timelineMode !is Timeline.Mode.Thread && event.threadInfo is TimelineItemThreadInfo.ThreadRoot) {
            ThreadSummaryView(
                modifier = if (event.isMine) {
                    Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp)
                } else {
                    if (timelineRoomInfo.isDm) Modifier else Modifier.padding(start = 16.dp)
                }
                    .padding(top = 2.dp)
                    .offset(selectionContentOffset)
                    .hiddenFromAccessibilityWhile(isSelectionToggle),
                threadSummary = event.threadInfo.summary,
                latestEventText = event.threadInfo.latestEventText,
                isOutgoing = event.isMine,
                onClick = {
                    event.eventId?.let {
                        eventSink(TimelineEvent.OpenThread(it.toThreadId(), null))
                    }
                }
            )
        }

        // Read receipts / Send state
        val sendState = event.pendingSend()?.second
        TimelineItemReadReceiptView(
            state = ReadReceiptViewState(
                sendState = sendState,
                isLastOutgoingMessage = isLastOutgoingMessage,
                renderReadReceipts = timelineRoomInfo.renderReadReceipts,
                knownReceipts = event.readReceiptState.receipts,
            ),
            onReadReceiptsClick = { onReadReceiptClick(event) },
            // Not slid: the read receipts sit outside the slid content, so they stay pinned to the end edge in selection mode.
            modifier = Modifier
                .padding(top = 4.dp)
                .hiddenFromAccessibilityWhile(isSelectionToggle)
        )
    }
}

/**
 * Hides this element and its descendants from screen readers while [hidden] is true.
 *
 * A clickable descendant stays an accessibility node of its own even though the row merges its descendants, so while the row acts as a selection toggle
 * it would otherwise still be reachable and activatable by a screen reader. Reactions already hide themselves this way at all times.
 */
private fun Modifier.hiddenFromAccessibilityWhile(hidden: Boolean): Modifier = if (hidden) {
    semantics { hideFromAccessibility() }
} else {
    this
}

/**
 * Exposes the gestures of a timeline item — long press for the action list, swipe for a reply — as TalkBack actions, since neither gesture is reachable
 * with a screen reader enabled.
 */
@Composable
private fun rememberTimelineItemAccessibilityActions(
    isEnabled: Boolean,
    canReply: Boolean,
    onLongClick: () -> Unit,
    onSwipeToReply: () -> Unit,
): List<CustomAccessibilityAction> {
    val messageActionsLabel = stringResource(CommonStrings.common_message_actions)
    val replyLabel = stringResource(CommonStrings.action_reply)
    val latestOnLongClick by rememberUpdatedState(onLongClick)
    val latestOnSwipeToReply by rememberUpdatedState(onSwipeToReply)
    return remember(isEnabled, canReply, messageActionsLabel, replyLabel) {
        buildList {
            if (!isEnabled) return@buildList
            add(
                CustomAccessibilityAction(messageActionsLabel) {
                    latestOnLongClick()
                    true
                }
            )
            if (canReply) {
                add(
                    CustomAccessibilityAction(replyLabel) {
                        latestOnSwipeToReply()
                        true
                    }
                )
            }
        }
    }
}

@Composable
private fun ThreadSummaryView(
    threadSummary: ThreadSummary,
    latestEventText: String?,
    isOutgoing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        Row(
            modifier = Modifier
                .then(if (!isOutgoing) Modifier.padding(start = 16.dp) else Modifier)
                .graphicsLayer {
                    shape = RoundedCornerShape(8.dp)
                    clip = true
                }
                .background(MessageEventBubbleDefaults.backgroundBubbleColor(isOutgoing))
                .niceClickable(onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .widthIn(max = (maxWidth - 24.dp) * MessageEventBubbleDefaults.BUBBLE_WIDTH_RATIO),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = CompoundIcons.ThreadsSolid(),
                contentDescription = null,
                tint = ElementTheme.colors.iconSecondary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = pluralStringResource(CommonPlurals.common_replies, threadSummary.numberOfReplies.toInt(), threadSummary.numberOfReplies),
                style = ElementTheme.typography.fontBodySmMedium,
                color = ElementTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.width(8.dp))

            threadSummary.latestEvent.dataOrNull()?.let { latestEvent ->
                val avatarData = AvatarData(
                    id = latestEvent.senderId.value,
                    name = latestEvent.senderProfile.getDisplayName(),
                    url = latestEvent.senderProfile.getAvatarUrl(),
                    size = AvatarSize.TimelineThreadLatestEventSender,
                )
                Avatar(
                    avatarData = avatarData,
                    avatarType = AvatarType.User,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = latestEvent.senderProfile.getDisambiguatedDisplayName(latestEvent.senderId),
                    style = ElementTheme.typography.fontBodySmMedium,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.width(4.dp))

                latestEventText?.let {
                    Text(
                        text = it,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Impact ViewConfiguration.touchSlop by [sensitivityFactor].
 * Inspired from https://issuetracker.google.com/u/1/issues/269627294.
 * @param sensitivityFactor the factor to multiply the touchSlop by. The highest value, the more the user will
 * have to drag to start the drag.
 * @param content the content to display.
 */
@Composable
private fun SwipeSensitivity(
    sensitivityFactor: Float,
    content: @Composable () -> Unit,
) {
    val current = LocalViewConfiguration.current
    CompositionLocalProvider(
        LocalViewConfiguration provides object : ViewConfiguration by current {
            override val touchSlop: Float
                get() = current.touchSlop * sensitivityFactor
        }
    ) {
        content()
    }
}

@Composable
private fun TimelineItemEventRowContent(
    event: TimelineItem.Event,
    timelineMode: Timeline.Mode,
    timelineProtectionState: TimelineProtectionState,
    timelineRoomInfo: TimelineRoomInfo,
    interactionSource: MutableInteractionSource,
    onContentClick: () -> Unit,
    onLongClick: () -> Unit,
    inReplyToClick: () -> Unit,
    onUserDataClick: () -> Unit,
    onReactionClick: (emoji: String) -> Unit,
    onReactionLongClick: (emoji: String) -> Unit,
    onMoreReactionsClick: (event: TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectionData: TimelineItemSelectionData = TimelineItemSelectionData(),
    selectionIndicatorOffset: Density.() -> IntOffset = NoSelectionOffset,
    eventContentView: @Composable (Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit,
) {
    fun ConstrainScope.linkStartOrEnd(event: TimelineItem.Event) = if (event.isMine) {
        end.linkTo(parent.end)
    } else {
        start.linkTo(parent.start)
    }

    ConstraintLayout(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
    ) {
        val (
            sender,
            message,
            reactions,
            pinIcon,
            selectionIndicator,
        ) = createRefs()

        if (selectionData.canBeSelected) {
            SelectionIndicator(
                selected = selectionData.isSelected,
                modifier = Modifier
                    .constrainAs(selectionIndicator) {
                        start.linkTo(parent.start)
                        top.linkTo(message.top)
                        bottom.linkTo(message.bottom)
                    }
                    .offset(selectionIndicatorOffset)
                    .padding(start = 16.dp)
                    .size(24.dp),
            )
        }

        // Sender
        if (event.showSenderInformation && !timelineRoomInfo.isDm) {
            MessageSenderInformation(
                event.senderId,
                event.senderProfile,
                event.senderAvatar,
                onUserDataClick,
                Modifier
                    .constrainAs(sender) {
                        top.linkTo(parent.top)
                        // Required for correct RTL layout
                        start.linkTo(parent.start)
                    }
                    .padding(horizontal = 16.dp)
                    .zIndex(1f),
            )
        }

        val currentContentValidationState by rememberEventContentValidationState(eventId = event.eventId, needsValidation = event.content.isMedia)
            .collectOverallState()
        val needsInvalidContentCustomisations =
            // Gallery events should not apply the custom bubble color, instead each item will apply some custom color if needed
            event.content !is TimelineItemGalleryContent &&
                event.content !is TimelineItemAttachmentsContent &&
                currentContentValidationState.hasError() &&
                event.content.isMedia

        // If the event has a dangerous media content we need to set custom message bubble background and border colors
        val themeColors = ElementTheme.colors
        val (dangerousContentBubbleColor, borderColor) = remember(themeColors.isLight, needsInvalidContentCustomisations, event.content.type) {
            val background = themeColors.bgCriticalSubtle.takeIf { needsInvalidContentCustomisations }
            val border = themeColors.borderCriticalSubtle.takeIf { needsInvalidContentCustomisations }
            background to border
        }

        // Message bubble
        val bubbleState = BubbleState(
            groupPosition = event.groupPosition,
            isMine = event.isMine,
            timelineRoomInfo = timelineRoomInfo,
        )
        MessageEventBubble(
            modifier = Modifier
                .constrainAs(message) {
                    val topMargin = if (bubbleState.cutTopStart) {
                        NEGATIVE_MARGIN_FOR_BUBBLE
                    } else {
                        0.dp
                    }
                    top.linkTo(sender.bottom, margin = topMargin)
                    if (event.isMine) {
                        end.linkTo(parent.end, margin = 16.dp)
                    } else {
                        val startMargin = if (timelineRoomInfo.isDm) 16.dp else 16.dp + BUBBLE_INCOMING_OFFSET
                        start.linkTo(parent.start, margin = startMargin)
                    }
                },
            state = bubbleState,
            interactionSource = interactionSource,
            onClick = onContentClick,
            onLongClick = onLongClick,
            customBackgroundColor = dangerousContentBubbleColor,
            borderColor = borderColor,
        ) {
            MessageEventBubbleContent(
                event = event,
                timelineMode = timelineMode,
                timelineProtectionState = timelineProtectionState,
                onMessageLongClick = onLongClick,
                inReplyToClick = inReplyToClick,
                eventSink = eventSink,
                eventContentView = eventContentView,
            )
        }

        // Pin icon
        val isEventPinned = timelineRoomInfo.pinnedEventIds.contains(event.eventId)
        if (isEventPinned) {
            Icon(
                imageVector = CompoundIcons.PinSolid(),
                contentDescription = stringResource(CommonStrings.common_pinned),
                tint = ElementTheme.colors.iconTertiary,
                modifier = Modifier
                    .padding(1.dp)
                    .size(16.dp)
                    .constrainAs(pinIcon) {
                        top.linkTo(message.top)
                        if (event.isMine) {
                            end.linkTo(message.start, margin = 8.dp)
                        } else {
                            start.linkTo(message.end, margin = 8.dp)
                        }
                    }
            )
        }

        // Reactions
        if (event.reactionsState.reactions.isNotEmpty()) {
            TimelineItemReactionsView(
                reactionsState = event.reactionsState,
                userCanSendReaction = timelineRoomInfo.userHasPermissionToSendReaction,
                isOutgoing = event.isMine,
                onReactionClick = onReactionClick,
                onReactionLongClick = onReactionLongClick,
                onMoreReactionsClick = { onMoreReactionsClick(event) },
                modifier = Modifier
                    .constrainAs(reactions) {
                        top.linkTo(message.bottom, margin = (-4).dp)
                        linkStartOrEnd(event)
                    }
                    .zIndex(1f)
                    .padding(
                        // Note: due to the applied constraints, start is left for other's message and right for mine
                        // In design we want a offset of 6.dp compare to the bubble, so start is 22.dp (16 + 6)
                        start = when {
                            event.isMine -> 22.dp
                            timelineRoomInfo.isDm -> 22.dp
                            else -> 22.dp + BUBBLE_INCOMING_OFFSET
                        },
                        end = 16.dp
                    )
            )
        }
    }
}

@Composable
private fun MessageSenderInformation(
    senderId: UserId,
    senderProfile: ProfileDetails,
    senderAvatar: AvatarData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarColors = AvatarColorsProvider.provide(senderAvatar.id)
    Row(
        modifier = modifier
            // Add external clickable modifier with no indicator so the touch target is larger than just the display name
            .clickable(onClick = onClick, enabled = true, interactionSource = remember { MutableInteractionSource() }, indication = null)
            .clearAndSetSemantics {
                hideFromAccessibility()
            }
    ) {
        Avatar(
            modifier = Modifier
                .testTag(TestTags.timelineItemSenderAvatar)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            avatarData = senderAvatar,
            avatarType = AvatarType.User,
        )
        SenderName(
            modifier = Modifier
                .testTag(TestTags.timelineItemSenderName)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp),
            senderId = senderId,
            senderProfile = senderProfile,
            senderNameMode = SenderNameMode.Timeline(avatarColors.foreground),
        )
    }
}

@Suppress("MultipleEmitters") // False positive
@Composable
private fun MessageEventBubbleContent(
    event: TimelineItem.Event,
    timelineMode: Timeline.Mode,
    timelineProtectionState: TimelineProtectionState,
    onMessageLongClick: () -> Unit,
    inReplyToClick: () -> Unit,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    @SuppressLint("ModifierParameter")
    // need to rename this modifier to prevent linter false positives
    @Suppress("ModifierNaming")
    bubbleModifier: Modifier = Modifier,
    eventContentView: @Composable (Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit,
) {
    // Long clicks are not not automatically propagated from a `clickable`
    // to its `combinedClickable` parent so we do it manually
    fun onTimestampLongClick() = onMessageLongClick()

    @Composable
    fun ThreadDecoration(
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.height(14.dp),
                imageVector = CompoundIcons.Threads(),
                contentDescription = null,
                tint = ElementTheme.colors.iconSecondary,
            )
            Text(
                text = stringResource(CommonStrings.common_thread),
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textPrimary,
                modifier = Modifier.clearAndSetSemantics { }
            )
        }
    }

    @Composable
    fun WithTimestampLayout(
        timestampPosition: TimestampPosition,
        eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
        modifier: Modifier = Modifier,
        canShrinkContent: Boolean = false,
        content: @Composable (onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit) -> Unit,
    ) {
        @Suppress("NAME_SHADOWING")
        val content = remember { movableContentOf(content) }
        when (timestampPosition) {
            TimestampPosition.Overlay ->
                Box(modifier, contentAlignment = Alignment.Center) {
                    content {}
                    TimelineEventTimestampView(
                        event = event,
                        eventSink = eventSink,
                        modifier = Modifier
                            // Outer padding
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .background(ElementTheme.colors.bgSubtleSecondary, RoundedCornerShape(10.0.dp))
                            .align(Alignment.BottomEnd)
                            // Inner padding
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            TimestampPosition.Aligned -> @Composable {
                val originalLayoutDirection = LocalLayoutDirection.current
                // Detect if the direction of the text content (if any) does not match the layout direction, to place the content and timestamp correctly
                val contentDirection = if (event.content is TimelineItemTextContent) {
                    remember(event.content.body) {
                        when (TextDirection.detect(event.content.body)) {
                            TextDirection.Ltr, TextDirection.ContentOrLtr -> LayoutDirection.Ltr
                            TextDirection.Rtl, TextDirection.ContentOrRtl -> LayoutDirection.Rtl
                            else -> originalLayoutDirection
                        }
                    }
                } else {
                    originalLayoutDirection
                }

                CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                    ContentAvoidingLayout(
                        modifier = modifier,
                        // The spacing is negative to make the content overlap the empty space at the start of the timestamp
                        spacing = (-4).dp,
                        overlayOffset = DpOffset(0.dp, -1.dp),
                        shrinkContent = canShrinkContent,
                        content = { content(this::onContentLayoutChange) },
                        overlay = {
                            // Use the original layout direction for the timestamp
                            CompositionLocalProvider(LocalLayoutDirection provides originalLayoutDirection) {
                                TimelineEventTimestampView(
                                    event = event,
                                    eventSink = eventSink,
                                    isLayoutDirectionMismatched = originalLayoutDirection != contentDirection,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )
                }
            }
            TimestampPosition.Below ->
                Column(modifier) {
                    content {}
                    TimelineEventTimestampView(
                        event = event,
                        eventSink = eventSink,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            TimestampPosition.Hidden -> Box(modifier) { content {} }
        }
    }

    /** Groups the different components in a Column with some space between them. */
    @Composable
    fun CommonLayout(
        timestampPosition: TimestampPosition,
        showThreadDecoration: Boolean,
        paddingBehaviour: ContentPadding,
        inReplyToDetails: InReplyToDetails?,
        modifier: Modifier = Modifier,
        canShrinkContent: Boolean = false,
    ) {
        val timestampLayoutModifier =
            if (inReplyToDetails != null && timestampPosition == TimestampPosition.Overlay) {
                Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            } else {
                Modifier
            }

        val topPadding = if (inReplyToDetails != null) 0.dp else 8.dp
        val contentModifier = when (paddingBehaviour) {
            ContentPadding.Textual ->
                Modifier.padding(start = 12.dp, end = 12.dp, top = topPadding, bottom = 8.dp)
            ContentPadding.Media -> {
                if (inReplyToDetails == null) {
                    Modifier
                } else {
                    Modifier.clip(RoundedCornerShape(10.dp))
                }
            }
            ContentPadding.CaptionedMedia ->
                Modifier.padding(start = 8.dp, end = 8.dp, top = topPadding, bottom = 8.dp)
            ContentPadding.InvalidContent -> Modifier.padding(top = topPadding, bottom = 8.dp)
        }

        val threadDecoration = @Composable {
            if (showThreadDecoration) {
                ThreadDecoration(modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp))
            }
        }
        val contentWithTimestamp = @Composable {
            WithTimestampLayout(
                timestampPosition = timestampPosition,
                eventSink = eventSink,
                canShrinkContent = canShrinkContent,
                modifier = timestampLayoutModifier.semantics(mergeDescendants = false) {
                    isTraversalGroup = true
                    traversalIndex = -1f
                },
                content = { onContentLayoutChange ->
                    eventContentView(contentModifier, onContentLayoutChange)
                }
            )
        }

        val inReplyTo = @Composable { inReplyTo: InReplyToDetails ->
            val currentContentValidationState by rememberEventContentValidationState(eventId = inReplyTo.eventId(), eventContent = inReplyTo.content())
                .collectOverallState()
            val topPadding = if (showThreadDecoration) 0.dp else 8.dp
            val shape = RoundedCornerShape(6.dp)
            val inReplyToModifier = Modifier
                .padding(top = topPadding, start = 8.dp, end = 8.dp)
                .clip(shape)

            val talkbackCompatModifier = when {
                // Use z-index to make the replied to text being read after the message
                // Usually, you'd use traversalIndex for that, but it's not working for some reason
                isTalkbackActive() -> inReplyToModifier.zIndex(1f)
                inReplyTo is InReplyToDetails.Error -> inReplyToModifier
                else -> inReplyToModifier.clickable(onClick = inReplyToClick)
            }

            val contentHasError = currentContentValidationState.hasError() || inReplyTo is InReplyToDetails.Error
            val borderColor = if (contentHasError) ElementTheme.colors.borderCriticalSubtle else ElementTheme.colors.separatorPrimary
            val backgroundColor = if (contentHasError) ElementTheme.colors.bgCriticalSubtle else ElementTheme.colors.bgCanvasDefault
            Box(
                modifier = talkbackCompatModifier
                    .border(1.dp, borderColor, shape)
                    .background(backgroundColor, shape)
                    .padding(4.dp)
            ) {
                val contentValidationState = rememberEventContentValidationState(eventId = inReplyTo.eventId(), eventContent = inReplyTo.content())
                val updatedEventSink by rememberUpdatedState(eventSink)
                LaunchedEffect(inReplyTo) {
                    val mediaSources = inReplyTo.content()?.mediaSources() ?: return@LaunchedEffect
                    updatedEventSink(TimelineEvent.ValidateMedia(inReplyTo.eventId(), mediaSources, contentValidationState))
                }
                InReplyToView(
                    inReplyTo = inReplyTo,
                    contentValidationValue = currentContentValidationState,
                    hideImage = timelineProtectionState.hideMediaContent(inReplyTo.eventId()),
                )
            }
        }
        if (inReplyToDetails != null) {
            // Use SubComposeLayout only if necessary as it can have consequences on the performance.
            EqualWidthColumn(spacing = 8.dp) {
                threadDecoration()
                inReplyTo(inReplyToDetails)
                contentWithTimestamp()
            }
        } else {
            Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                threadDecoration()
                contentWithTimestamp()
            }
        }
    }

    val contentValidationState by rememberEventContentValidationState(eventId = event.eventId, needsValidation = event.content.isMedia).collectOverallState()
    val needsInvalidContentLayout =
        // Gallery events should not apply custom paddings or layout dispositions
        event.content !is TimelineItemGalleryContent &&
            event.content !is TimelineItemAttachmentsContent &&
            contentValidationState.hasError()

    val timestampPosition = if (needsInvalidContentLayout) {
        // The invalid content view will be displayed in all these cases, independent of the event content
        TimestampPosition.Aligned
    } else {
        when (val content = event.content) {
            is TimelineItemImageContent -> if (content.showCaption) TimestampPosition.Aligned else TimestampPosition.Overlay
            is TimelineItemVideoContent -> if (content.showCaption) TimestampPosition.Aligned else TimestampPosition.Overlay
            is TimelineItemGalleryContent -> if (content.showCaption) TimestampPosition.Aligned else TimestampPosition.Below
            is TimelineItemAttachmentsContent -> if (content.showCaption) TimestampPosition.Aligned else TimestampPosition.Below
            is TimelineItemStickerContent -> TimestampPosition.Overlay
            is TimelineItemLocationContent -> {
                val content = content.ensureActiveLiveLocation()
                val shouldHide = content.mode is TimelineItemLocationContent.Mode.Live &&
                    content.mode.isActive &&
                    content.mode.isOwnUser
                if (shouldHide) TimestampPosition.Hidden else TimestampPosition.Overlay
            }
            is TimelineItemPollContent -> TimestampPosition.Below
            else -> TimestampPosition.Default
        }
    }

    val paddingBehaviour = if (needsInvalidContentLayout) {
        ContentPadding.InvalidContent
    } else {
        when (event.content) {
            is TimelineItemImageContent -> if (event.content.showCaption) ContentPadding.CaptionedMedia else ContentPadding.Media
            is TimelineItemVideoContent -> if (event.content.showCaption) ContentPadding.CaptionedMedia else ContentPadding.Media
            is TimelineItemGalleryContent -> ContentPadding.CaptionedMedia
            is TimelineItemAttachmentsContent -> ContentPadding.CaptionedMedia
            is TimelineItemStickerContent,
            is TimelineItemLocationContent -> ContentPadding.Media
            else -> ContentPadding.Textual
        }
    }
    CommonLayout(
        showThreadDecoration = timelineMode !is Timeline.Mode.Thread && event.threadInfo is TimelineItemThreadInfo.ThreadResponse,
        timestampPosition = timestampPosition,
        paddingBehaviour = paddingBehaviour,
        inReplyToDetails = event.inReplyTo,
        canShrinkContent = event.content is TimelineItemVoiceContent,
        modifier = bubbleModifier,
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    senderDisplayName = "Sender with a super long name that should ellipsize",
                    isMine = isMine,
                    content = aTimelineItemTextContent(
                        body = "A long text which will be displayed on several lines and" +
                            " hopefully can be manually adjusted to test different behaviors."
                    ),
                    groupPosition = TimelineItemGroupPosition.First,
                ),
            )
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemImageContent(
                        aspectRatio = 2.5f
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithThreadSummaryPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    senderDisplayName = "Sender with a super long name that should ellipsize",
                    isMine = isMine,
                    content = aTimelineItemTextContent(
                        body = "A long text which will be displayed on several lines and" +
                            " hopefully can be manually adjusted to test different behaviors."
                    ),
                    groupPosition = TimelineItemGroupPosition.First,
                    threadInfo = TimelineItemThreadInfo.ThreadRoot(
                        latestEventText = "This is the latest message in the thread",
                        summary = ThreadSummary(
                            latestEvent = AsyncData.Success(
                                EmbeddedEventInfo(
                                    eventOrTransactionId = EventOrTransactionId.Event(EventId("\$event-id")),
                                    content = MessageContent(
                                        body = "This is the latest message in the thread",
                                        inReplyTo = null,
                                        isEdited = false,
                                        threadInfo = null,
                                        type = TextMessageType("This is the latest message in the thread", null)
                                    ),
                                    senderId = UserId("@user:id"),
                                    senderProfile = ProfileDetails.Ready(
                                        displayName = USER_NAME_ALICE,
                                        avatarUrl = null,
                                        displayNameAmbiguous = false,
                                        displayedStatus = null,
                                    ),
                                    timestamp = 0L,
                                )
                            ),
                            numberOfReplies = 20L,
                        )
                    )
                ),
                displayThreadSummaries = true,
            )
        }
    }
}

@PreviewWithExtraLargeHeight
@Composable
internal fun TimelineItemEventRowRtlContentPreview() = ElementPreview {
    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp),
            text = "LTR layout direction",
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontHeadingMdBold,
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            sequenceOf(false, true).forEach { isMine ->
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع يَسْتَغْرِب فِيهَ"
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.First,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع يَسْتَغْرِب فِيهَ",
                            isEdited = true,
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Middle,
                        messageShield = MessageShield.UnknownDevice(isCritical = true),
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع \nيَسْتَغْرِب فِيهَ"
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Middle,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع يَسْتَغْرِب فِيهَا اَلشَّاعِر أَنْ لَا يَبْكِي مِنْ أَلَم اَلْفِرَاق،" +
                                " وَيَصِف حَالَة اَلْمُودِعِينَ"
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Last,
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp),
            text = "RTL layout direction",
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontHeadingMdBold,
        )

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            sequenceOf(false, true).forEach { isMine ->
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "ظَة وَدَاع يَسْتَغْرِب فِيهَا اَلشَّاعِر أَنْ لَا يَبْكِي مِنْ أَلَم اَلْفِرَاق،" +
                                " وَيَصِف حَالَة اَلْمُودِعِينَ",
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.First,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "Testing\nLTR Line\nBreaks.",
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Middle,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "Testing a very long LTR text in an RTL layout."
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Middle,
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "Testing LTR in RTL layout.",
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Last,
                        messageShield = MessageShield.UnknownDevice(isCritical = true),
                    )
                )
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        senderDisplayName = "Sender with a super long name that should ellipsize",
                        isMine = isMine,
                        content = aTimelineItemTextContent(
                            body = "Testing LTR in RTL layout.",
                            isEdited = true,
                        ),
                        timelineItemReactions = TimelineItemReactions(persistentListOf()),
                        groupPosition = TimelineItemGroupPosition.Last,
                        messageShield = MessageShield.UnknownDevice(isCritical = true),
                    )
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ThreadSummaryViewPreview() {
    ElementPreview {
        val body = "This is the latest message in the thread"
        val threadSummary = ThreadSummary(
            AsyncData.Success(
                EmbeddedEventInfo(
                    eventOrTransactionId = EventOrTransactionId.Event(EventId("\$event-id")),
                    content = MessageContent(
                        body = body,
                        inReplyTo = null,
                        isEdited = false,
                        threadInfo = null,
                        type = TextMessageType(body, null)
                    ),
                    senderId = UserId("@user:id"),
                    senderProfile = ProfileDetails.Ready(
                        displayName = USER_NAME_ALICE,
                        avatarUrl = null,
                        displayNameAmbiguous = true,
                        displayedStatus = null,
                    ),
                    timestamp = 0L,
                )
            ),
            numberOfReplies = 12,
        )

        ThreadSummaryView(
            threadSummary = threadSummary,
            latestEventText = "Some event with a very long text that should get clipped",
            isOutgoing = true,
            onClick = {},
        )
    }
}
