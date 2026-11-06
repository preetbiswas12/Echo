/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.compound.theme.ElementTheme
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.location.api.LiveLocationSharingBanner
import com.echo.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerEvent
import com.echo.features.messages.impl.actionlist.ActionListEvent
import com.echo.features.messages.impl.actionlist.ActionListView
import com.echo.features.messages.impl.actionlist.model.TimelineItemAction
import com.echo.features.messages.impl.crypto.identity.IdentityChangeStateView
import com.echo.features.messages.impl.link.LinkEvent
import com.echo.features.messages.impl.link.LinkView
import com.echo.features.messages.impl.messagecomposer.AttachmentsBottomSheet
import com.echo.features.messages.impl.messagecomposer.DisabledComposerView
import com.echo.features.messages.impl.messagecomposer.MessageComposerEvent
import com.echo.features.messages.impl.messagecomposer.MessageComposerView
import com.echo.features.messages.impl.messagecomposer.suggestions.SuggestionsPickerView
import com.echo.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import com.echo.features.messages.impl.pinned.banner.PinnedMessagesBannerView
import com.echo.features.messages.impl.pinned.banner.PinnedMessagesBannerViewDefaults
import com.echo.features.messages.impl.timeline.FOCUS_ON_PINNED_EVENT_DEBOUNCE_DURATION_IN_MILLIS
import com.echo.features.messages.impl.timeline.SelectionAction
import com.echo.features.messages.impl.timeline.SelectionState
import com.echo.features.messages.impl.timeline.TimelineEvent
import com.echo.features.messages.impl.timeline.TimelineView
import com.echo.features.messages.impl.timeline.aGroupedEvents
import com.echo.features.messages.impl.timeline.aTimelineItemDaySeparator
import com.echo.features.messages.impl.timeline.aTimelineItemEvent
import com.echo.features.messages.impl.timeline.aTimelineItemList
import com.echo.features.messages.impl.timeline.aTimelineState
import com.echo.features.messages.impl.timeline.canApplyTo
import com.echo.features.messages.impl.timeline.components.CallMenuItem
import com.echo.features.messages.impl.timeline.components.customreaction.CustomReactionEvent
import com.echo.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryEvent
import com.echo.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryView
import com.echo.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheet
import com.echo.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetEvent
import com.echo.features.messages.impl.timeline.isSelectionModeActive
import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.features.messages.impl.timeline.model.TimelineItemGroupPosition
import com.echo.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import com.echo.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import com.echo.features.messages.impl.timeline.selectedCount
import com.echo.features.messages.impl.timeline.sendfailure.SendFailureDialogView
import com.echo.features.messages.impl.topbars.MessagesViewTopBar
import com.echo.features.messages.impl.topbars.SelectionModeTopBar
import com.echo.features.messages.impl.topbars.ThreadTopBar
import com.echo.features.messages.impl.voicemessages.composer.VoiceMessagePermissionRationaleDialog
import com.echo.features.messages.impl.voicemessages.composer.VoiceMessageSendingFailedDialog
import com.echo.features.roomcall.api.RoomCallState
import com.echo.libraries.androidutils.ui.hideKeyboard
import com.echo.libraries.designsystem.atomic.molecules.ComposerAlertMolecule
import com.echo.libraries.designsystem.components.ExpandableBottomSheetLayout
import com.echo.libraries.designsystem.components.ExpandableBottomSheetLayoutState
import com.echo.libraries.designsystem.components.dialogs.ConfirmationDialog
import com.echo.libraries.designsystem.components.dialogs.TextFieldDialog
import com.echo.libraries.designsystem.components.rememberExpandableBottomSheetLayoutState
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.text.toAnnotatedString
import com.echo.libraries.designsystem.text.toDp
import com.echo.libraries.designsystem.theme.components.BottomSheetDragHandle
import com.echo.libraries.designsystem.theme.components.Icon
import com.echo.libraries.designsystem.theme.components.Scaffold
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.designsystem.utils.HideKeyboardWhenDisposed
import com.echo.libraries.designsystem.utils.KeepScreenOn
import com.echo.libraries.designsystem.utils.OnLifecycleEvent
import com.echo.libraries.designsystem.utils.scaffoldScrollableContentInsets
import com.echo.libraries.designsystem.utils.snackbar.SnackbarHost
import com.echo.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.encryption.identity.IdentityState
import com.echo.libraries.matrix.api.room.tombstone.SuccessorRoom
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.api.timeline.item.event.LocalEventSendState
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import com.echo.libraries.matrix.ui.media.contentvalidation.LocalEventContentValidationState
import com.echo.libraries.textcomposer.model.TextEditorState
import com.echo.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.link.Link
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MessagesView(
    state: MessagesState,
    onBackClick: () -> Unit,
    onRoomDetailsClick: () -> Unit,
    onEventContentClick: (isLive: Boolean, event: TimelineItem.Event) -> Boolean,
    onGalleryEventItemClick: (isLive: Boolean, event: TimelineItem.Event, index: Int) -> Boolean,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String, Boolean) -> Unit,
    onSendLocationClick: () -> Unit,
    onCreatePollClick: () -> Unit,
    onJoinCallClick: (isAudioCall: Boolean) -> Unit,
    onViewAllPinnedMessagesClick: () -> Unit,
    onThreadsListClick: () -> Unit,
    knockRequestsBannerView: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    forceJumpToBottomVisibility: Boolean = false,
    customReactionBottomSheet: @Composable () -> Unit,
) {
    val eventContentValidationState = LocalEventContentValidationState.current

    OnLifecycleEvent { _, event ->
        state.voiceMessageComposerState.eventSink(VoiceMessageComposerEvent.LifecycleEvent(event))
    }

    KeepScreenOn(state.voiceMessageComposerState.keepScreenOn)

    HideKeyboardWhenDisposed()

    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    var maxComposerHeightPx by remember { mutableIntStateOf(120) }

    // This is needed because the composer is inside an AndroidView that can't be affected by the FocusManager in Compose
    val localView = LocalView.current

    fun hidingKeyboard(block: () -> Unit) {
        localView.hideKeyboard()
        block()
    }

    // Entering selection mode collapses the composer, so dismiss the keyboard too. hideKeyboard
    // triggers the IME hide animation, keeping it in sync with the composer collapse.
    LaunchedEffect(state.timelineState.isSelectionModeActive) {
        if (state.timelineState.isSelectionModeActive) {
            localView.hideKeyboard()
        }
    }

    BackHandler(enabled = state.timelineState.isSelectionModeActive) {
        state.timelineState.eventSink(TimelineEvent.ExitSelectionMode)
    }

    fun onContentClick(event: TimelineItem.Event) {
        Timber.v("onMessageClick= ${event.id}")
        val eventId = event.eventId
        if (eventId != null && eventContentValidationState[eventId].getCurrentOverallState() != ContentValidationValue.Valid) return

        val hideKeyboard = onEventContentClick(state.timelineState.isLive, event)
        if (hideKeyboard) {
            localView.hideKeyboard()
        }
    }

    fun onMessageLongClick(event: TimelineItem.Event) {
        Timber.v("OnMessageLongClicked= ${event.id}")
        hidingKeyboard {
            state.actionListState.eventSink(
                ActionListEvent.ComputeForMessage(
                    event = event,
                    userEventPermissions = state.userEventPermissions,
                )
            )
        }
    }

    fun onActionSelected(action: TimelineItemAction, event: TimelineItem.Event) {
        state.eventSink(MessagesEvent.HandleAction(action, event))
    }

    fun onEmojiReactionClick(emoji: String, event: TimelineItem.Event) {
        state.eventSink(MessagesEvent.ToggleReaction(emoji, event.eventOrTransactionId))
    }

    fun onEmojiReactionLongClick(emoji: String, event: TimelineItem.Event) {
        if (event.eventId == null) return
        state.reactionSummaryState.eventSink(ReactionSummaryEvent.ShowReactionSummary(event.eventId, event.reactionsState.reactions, emoji))
    }

    fun onMoreReactionsClick(event: TimelineItem.Event) {
        state.customReactionState.eventSink(CustomReactionEvent.ShowCustomReactionSheet(event))
    }

    val expandableState = rememberExpandableBottomSheetLayoutState()
    ExpandableBottomSheetLayout(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding()
            .onSizeChanged { size ->
                // Let the composer takes at max half of the available height.
                // The value will be different if the soft keyboard is displayed
                // or not.
                maxComposerHeightPx = (size.height * 0.5f).toInt()
            },
        content = {
            Scaffold(
                contentWindowInsets = scaffoldScrollableContentInsets,
                topBar = {
                    when {
                        state.timelineState.isSelectionModeActive -> {
                            val selection = state.timelineState.selectionState as SelectionState.Active
                            SelectionModeTopBar(
                                selectedCount = state.timelineState.selectedCount,
                                action = selection.action,
                                onCancelClick = {
                                    state.timelineState.eventSink(TimelineEvent.ExitSelectionMode)
                                },
                                onConfirmClick = {
                                    state.eventSink(MessagesEvent.ConfirmSelectionAction)
                                },
                            )
                        }
                        state.timelineState.timelineMode is Timeline.Mode.Thread -> {
                            ThreadTopBar(
                                roomName = state.roomName,
                                roomAvatarData = state.roomAvatar,
                                heroes = state.heroes,
                                isTombstoned = state.isTombstoned,
                                onBackClick = onBackClick,
                            )
                        }
                        else -> {
                            MessagesViewTopBar(
                                roomName = state.roomName,
                                roomAvatar = state.roomAvatar,
                                isTombstoned = state.isTombstoned,
                                heroes = state.heroes,
                                dmUserIdentityState = state.dmUserVerificationState,
                                sharedHistoryIcon = state.topBarSharedHistoryIcon,
                                dmUserStatus = state.dmUserStatus,
                                onBackClick = { hidingKeyboard { onBackClick() } },
                                onRoomDetailsClick = { hidingKeyboard { onRoomDetailsClick() } },
                                menuActions = {
                                    MessagesMenuActions(
                                        displayThreads = state.timelineState.timelineMode !is Timeline.Mode.Thread && state.threads.hasThreads,
                                        roomCallState = state.roomCallState,
                                        onJoinCallClick = onJoinCallClick,
                                        onThreadsListClick = onThreadsListClick
                                    )
                                }
                            )
                        }
                    }
                },
                content = { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .consumeWindowInsets(padding)
                    ) {
                        MessagesViewContent(
                            state = state,
                            onContentClick = ::onContentClick,
                            onGalleryItemClick = { event, index ->
                                val hideKeyboard = onGalleryEventItemClick(
                                    state.timelineState.isLive,
                                    event,
                                    index,
                                )
                                if (hideKeyboard) {
                                    localView.hideKeyboard()
                                }
                            },
                            onMessageLongClick = ::onMessageLongClick,
                            onUserDataClick = {
                                hidingKeyboard {
                                    state.eventSink(MessagesEvent.OnUserClicked(it))
                                }
                            },
                            onLinkClick = { link, customTab ->
                                if (customTab) {
                                    onLinkClick(link.url, true)
                                    // Do not check those links, they are internal link only
                                } else {
                                    state.linkState.eventSink(LinkEvent.OnLinkClick(link))
                                }
                            },
                            onReactionClick = ::onEmojiReactionClick,
                            onReactionLongClick = ::onEmojiReactionLongClick,
                            onMoreReactionsClick = ::onMoreReactionsClick,
                            onReadReceiptClick = { event ->
                                state.readReceiptBottomSheetState.eventSink(ReadReceiptBottomSheetEvent.EventSelected(event))
                            },
                            onSendLocationClick = onSendLocationClick,
                            onCreatePollClick = onCreatePollClick,
                            onSwipeToReply = { targetEvent ->
                                state.eventSink(MessagesEvent.HandleAction(TimelineItemAction.Reply, targetEvent))
                            },
                            onJoinCallClick = onJoinCallClick,
                            forceJumpToBottomVisibility = forceJumpToBottomVisibility,
                            onViewAllPinnedMessagesClick = onViewAllPinnedMessagesClick,
                            knockRequestsBannerView = knockRequestsBannerView,
                        )

                        SuggestionsPickerView(
                            modifier = Modifier
                                .shadow(10.dp)
                                .background(ElementTheme.colors.bgCanvasDefault)
                                .align(Alignment.BottomStart)
                                .heightIn(max = 230.dp),
                            roomId = state.roomId,
                            roomName = state.roomName,
                            roomAvatarData = state.roomAvatar,
                            suggestions = state.composerState.suggestions,
                            onSelectSuggestion = {
                                state.composerState.eventSink(MessageComposerEvent.InsertSuggestion(it))
                            }
                        )
                    }
                },
                snackbarHost = {
                    SnackbarHost(
                        snackbarHostState,
                        modifier = Modifier.navigationBarsPadding()
                    )
                },
            )
        },
        bottomSheetContent = {
            MessagesViewComposerBottomSheetContents(
                state = state,
                onLinkClick = { url, customTab -> onLinkClick(url, customTab) },
                onRoomSuccessorClick = { roomId ->
                    state.timelineState.eventSink(TimelineEvent.NavigateToPredecessorOrSuccessorRoom(roomId = roomId))
                },
            )
        },
        sheetDragHandle = @Composable { toggleAction ->
            if (state.composerState.showTextFormatting) {
                val expandA11yLabel = stringResource(CommonStrings.a11y_expand_message_text_field)
                val collapseA11yLabel = stringResource(CommonStrings.a11y_collapse_message_text_field)
                BottomSheetDragHandle(
                    modifier = Modifier.semantics {
                        role = Role.Button
                        // Accessibility action to toggle the bottom sheet state
                        val label = when (expandableState.position) {
                            ExpandableBottomSheetLayoutState.Position.COLLAPSED, ExpandableBottomSheetLayoutState.Position.DRAGGING -> expandA11yLabel
                            ExpandableBottomSheetLayoutState.Position.EXPANDED -> collapseA11yLabel
                        }
                        onClick(label) {
                            toggleAction()
                            true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    // Ensure that the bottom sheet is collapsed
                    if (expandableState.position == ExpandableBottomSheetLayoutState.Position.EXPANDED) {
                        toggleAction()
                    }
                }
            }
        },
        isSwipeGestureEnabled = state.composerState.showTextFormatting,
        state = expandableState,
        sheetShape = if (state.composerState.showTextFormatting || state.composerState.suggestions.isNotEmpty()) {
            MaterialTheme.shapes.large
        } else {
            RectangleShape
        },
        maxBottomSheetContentHeight = maxComposerHeightPx.toDp(),
    )

    var endPollConfirmingEvent: TimelineItem.Event? by remember { mutableStateOf(null) }

    if (endPollConfirmingEvent != null) {
        ConfirmationDialog(
            content = stringResource(id = CommonStrings.common_poll_end_confirmation),
            onSubmitClick = {
                endPollConfirmingEvent?.let { event ->
                    onActionSelected(TimelineItemAction.EndPoll, event)
                }
                endPollConfirmingEvent = null
            },
            onDismiss = { endPollConfirmingEvent = null },
        )
    }

    ActionListView(
        state = state.actionListState,
        onSelectAction = { action: TimelineItemAction, event: TimelineItem.Event ->
            if (action == TimelineItemAction.EndPoll) {
                endPollConfirmingEvent = event
            } else {
                onActionSelected(action, event)
            }
        },
        onCustomReactionClick = { event ->
            state.customReactionState.eventSink(CustomReactionEvent.ShowCustomReactionSheet(event))
        },
        onEmojiReactionClick = ::onEmojiReactionClick,
        onVerifiedUserSendFailureClick = { event ->
            state.timelineState.eventSink(TimelineEvent.ComputeVerifiedUserSendFailure(event))
        },
    )

    customReactionBottomSheet()

    ReactionSummaryView(state = state.reactionSummaryState)
    ReadReceiptBottomSheet(
        state = state.readReceiptBottomSheetState,
        onUserDataClick = onUserDataClick,
    )
    ReinviteDialog(state = state)
    LinkView(
        onLinkValid = { link ->
            onLinkClick(link.url, false)
        },
        state = state.linkState,
    )

    SendFailureDialogView(
        sendFailureDialogState = state.timelineState.sendFailureDialogState,
        onDismiss = {
            state.timelineState.eventSink(TimelineEvent.HideSendFailureDialog)
        },
        onRetry = { event ->
            state.eventSink(
                MessagesEvent.HandleAction(
                    action = TimelineItemAction.RetrySending,
                    event = event,
                )
            )
            state.timelineState.eventSink(TimelineEvent.HideSendFailureDialog)
        },
        onRemoveMessage = { event ->
            state.eventSink(
                MessagesEvent.HandleAction(
                    action = TimelineItemAction.AbortSending,
                    event = event,
                )
            )
            state.timelineState.eventSink(TimelineEvent.HideSendFailureDialog)
        },
    )

    if (state.redactEventAction is MessagesState.ConfirmingRedaction) {
        TextFieldDialog(
            title = stringResource(R.string.screen_room_confirm_removal_title),
            placeholder = stringResource(R.string.screen_room_confirm_removal_reason_placeholder),
            supportingText = stringResource(R.string.screen_room_confirm_removal_reason_supporting_text),
            value = null,
            submitText = stringResource(CommonStrings.action_remove),
            destructiveSubmit = true,
            onSubmit = { reason -> state.eventSink(MessagesEvent.ConfirmRedact(reason)) },
            onDismissRequest = { state.eventSink(MessagesEvent.CancelRedact) },
        )
    }
}

@Composable
internal fun RowScope.MessagesMenuActions(
    displayThreads: Boolean,
    roomCallState: RoomCallState,
    onJoinCallClick: (isAudioCall: Boolean) -> Unit,
    onThreadsListClick: () -> Unit,
) {
    if (displayThreads) {
        Icon(
            modifier = Modifier.clickable(enabled = true, onClick = onThreadsListClick),
            imageVector = CompoundIcons.ThreadsSolid(),
            contentDescription = stringResource(CommonStrings.common_threads),
        )
        Spacer(Modifier.width(8.dp))
    }
    CallMenuItem(
        roomCallState = roomCallState,
        onJoinCallClick = onJoinCallClick,
    )
    Spacer(Modifier.width(8.dp))
}

@Composable
private fun ReinviteDialog(state: MessagesState) {
    if (state.showReinvitePrompt) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_room_invite_again_alert_title),
            content = stringResource(id = R.string.screen_room_invite_again_alert_message),
            cancelText = stringResource(id = CommonStrings.action_cancel),
            submitText = stringResource(id = CommonStrings.action_invite),
            onSubmitClick = { state.eventSink(MessagesEvent.InviteDialogDismissed(InviteDialogAction.Invite)) },
            onDismiss = { state.eventSink(MessagesEvent.InviteDialogDismissed(InviteDialogAction.Cancel)) }
        )
    }
}

@Composable
private fun MessagesViewContent(
    state: MessagesState,
    onContentClick: (TimelineItem.Event) -> Unit,
    onUserDataClick: (MatrixUser) -> Unit,
    onLinkClick: (Link, Boolean) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    onMessageLongClick: (TimelineItem.Event) -> Unit,
    onGalleryItemClick: ((TimelineItem.Event, Int) -> Unit),
    onSendLocationClick: () -> Unit,
    onCreatePollClick: () -> Unit,
    onViewAllPinnedMessagesClick: () -> Unit,
    onJoinCallClick: (isAudioCall: Boolean) -> Unit,
    forceJumpToBottomVisibility: Boolean,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
    knockRequestsBannerView: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        AttachmentsBottomSheet(
            state = state.composerState,
            onSendLocationClick = onSendLocationClick,
            onCreatePollClick = onCreatePollClick,
            enableTextFormatting = state.enableTextFormatting,
        )

        if (state.voiceMessageComposerState.showPermissionRationaleDialog) {
            VoiceMessagePermissionRationaleDialog(
                onContinue = {
                    state.voiceMessageComposerState.eventSink(VoiceMessageComposerEvent.AcceptPermissionRationale)
                },
                onDismiss = {
                    state.voiceMessageComposerState.eventSink(VoiceMessageComposerEvent.DismissPermissionsRationale)
                },
                appName = state.appName
            )
        }
        if (state.voiceMessageComposerState.showSendFailureDialog) {
            VoiceMessageSendingFailedDialog(
                onDismiss = { state.voiceMessageComposerState.eventSink(VoiceMessageComposerEvent.DismissSendFailureDialog) },
            )
        }

        Box {
            val scrollBehavior = PinnedMessagesBannerViewDefaults.rememberScrollBehavior(
                pinnedMessagesCount = (state.pinnedMessagesBannerState as? PinnedMessagesBannerState.Visible)?.pinnedMessagesCount() ?: 0,
            )
            val density = LocalDensity.current
            // Combined height of the banners overlaid above the timeline. Drives the floating
            // date badge offset so the badge sits below whichever banners are currently showing.
            var topBannersHeightDp by remember { mutableStateOf(0.dp) }

            TimelineView(
                state = state.timelineState,
                timelineProtectionState = state.timelineProtectionState,
                onUserDataClick = onUserDataClick,
                onLinkClick = { link -> onLinkClick(link, false) },
                onContentClick = onContentClick,
                onGalleryItemClick = onGalleryItemClick,
                onMessageLongClick = onMessageLongClick,
                onSwipeToReply = onSwipeToReply,
                onReactionClick = onReactionClick,
                onReactionLongClick = onReactionLongClick,
                onMoreReactionsClick = onMoreReactionsClick,
                onReadReceiptClick = onReadReceiptClick,
                onJoinCallClick = onJoinCallClick,
                forceJumpToBottomVisibility = forceJumpToBottomVisibility,
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                floatingDateTopOffset = topBannersHeightDp,
            )

            if (state.timelineState.timelineMode !is Timeline.Mode.Thread) {
                Column(
                    modifier = Modifier.onSizeChanged { topBannersHeightDp = with(density) { it.height.toDp() } },
                ) {
                    AnimatedVisibility(
                        visible = state.pinnedMessagesBannerState is PinnedMessagesBannerState.Visible && scrollBehavior.isVisible,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        fun focusOnPinnedEvent(eventId: EventId) {
                            state.timelineState.eventSink(
                                TimelineEvent.FocusOnEvent(eventId = eventId, debounce = FOCUS_ON_PINNED_EVENT_DEBOUNCE_DURATION_IN_MILLIS.milliseconds)
                            )
                        }
                        PinnedMessagesBannerView(
                            state = state.pinnedMessagesBannerState,
                            onClick = ::focusOnPinnedEvent,
                            onViewAllClick = onViewAllPinnedMessagesClick,
                        )
                    }
                    if (state.showLiveLocationShareBanner) {
                        LiveLocationSharingBanner(
                            onClick = { state.eventSink(MessagesEvent.ShowLiveLocationShare) },
                            onStopClick = { state.eventSink(MessagesEvent.StopLiveLocationShare) }
                        )
                    }
                }
            }

            knockRequestsBannerView()
        }
    }
}

@Composable
private fun MessagesViewComposerBottomSheetContents(
    state: MessagesState,
    onRoomSuccessorClick: (RoomId) -> Unit,
    onLinkClick: (String, Boolean) -> Unit,
) {
    val contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
    when {
        state.successorRoom != null -> {
            SuccessorRoomBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                roomSuccessor = state.successorRoom,
                onRoomSuccessorClick = onRoomSuccessorClick
            )
        }
        state.userEventPermissions.canSendMessage -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
            ) {
                // Do not show the identity change if user is composing a Rich message or is seeing suggestion(s).
                if (state.composerState.suggestions.isEmpty() &&
                    state.composerState.textEditorState is TextEditorState.Markdown) {
                    IdentityChangeStateView(
                        state = state.identityChangeState,
                        onLinkClick = onLinkClick,
                    )
                }
                val verificationViolation = state.identityChangeState.roomMemberIdentityStateChanges.firstOrNull {
                    it.identityState == IdentityState.VerificationViolation
                }
                if (verificationViolation != null) {
                    DisabledComposerView(modifier = Modifier.fillMaxWidth())
                } else {
                    MessageComposerView(
                        state = state.composerState,
                        voiceMessageState = state.voiceMessageComposerState,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        else -> {
            CantSendMessageBanner(Modifier.padding(contentPadding))
        }
    }
}

@Composable
private fun CantSendMessageBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ElementTheme.colors.bgSubtleSecondary)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.screen_room_timeline_no_permission_to_post),
            color = ElementTheme.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
        )
    }
}

@Composable
private fun SuccessorRoomBanner(
    roomSuccessor: SuccessorRoom,
    onRoomSuccessorClick: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    ComposerAlertMolecule(
        avatar = null,
        content = stringResource(R.string.screen_room_timeline_tombstoned_room_message).toAnnotatedString(),
        onSubmitClick = { onRoomSuccessorClick(roomSuccessor.roomId) },
        modifier = modifier,
        submitText = stringResource(R.string.screen_room_timeline_tombstoned_room_action)
    )
}

@PreviewsDayNight
@Composable
internal fun MessagesViewPreview(@PreviewParameter(MessagesStatePreviewParam::class) state: MessagesState) = ElementPreview {
    MessagesView(
        state = state,
        onBackClick = {},
        onRoomDetailsClick = {},
        onEventContentClick = { _, _ -> false },
        onGalleryEventItemClick = { _, _, _ -> false },
        onUserDataClick = {},
        onLinkClick = { _, _ -> },
        onSendLocationClick = {},
        onCreatePollClick = {},
        onJoinCallClick = {},
        onViewAllPinnedMessagesClick = { },
        forceJumpToBottomVisibility = true,
        knockRequestsBannerView = {},
        customReactionBottomSheet = {},
        onThreadsListClick = {},
    )
}

@PreviewsDayNight
@Composable
internal fun MessagesViewSelectionModePreview() = ElementPreview {
    val timelineItems = aTimelineItemList(aTimelineItemTextContent())
    val selectedEventIds = timelineItems
        .filterIsInstance<TimelineItem.Event>()
        .filter { SelectionAction.Forward.canApplyTo(it) }
        .take(2)
        .mapNotNull { it.eventId }
        .toImmutableSet()
    MessagesView(
        state = aMessagesState(
            timelineState = aTimelineState(
                timelineItems = timelineItems,
                selectionState = SelectionState.Active(SelectionAction.Forward, selectedEventIds),
            ),
        ),
        onBackClick = {},
        onRoomDetailsClick = {},
        onEventContentClick = { _, _ -> false },
        onGalleryEventItemClick = { _, _, _ -> false },
        onUserDataClick = {},
        onLinkClick = { _, _ -> },
        onSendLocationClick = {},
        onCreatePollClick = {},
        onJoinCallClick = {},
        onViewAllPinnedMessagesClick = { },
        forceJumpToBottomVisibility = true,
        knockRequestsBannerView = {},
        customReactionBottomSheet = {},
        onThreadsListClick = {},
    )
}

@Preview
@Composable
internal fun MessagesViewA11yPreview() = ElementPreview {
    val content = aTimelineItemTextContent(
        body = "A message content"
    )
    MessagesView(
        state = aMessagesState(
            roomName = "A DM with a very looong name",
            dmUserVerificationState = IdentityState.VerificationViolation,
            timelineState = aTimelineState(
                timelineItems = persistentListOf(
                    // 1 items with isMine = false
                    aTimelineItemEvent(
                        isMine = false,
                        content = content,
                        groupPosition = TimelineItemGroupPosition.None,
                        sendState = LocalEventSendState.Failed.Unknown("Message failed to send"),
                    ),
                    // A state event on top of it
                    aTimelineItemEvent(
                        isMine = false,
                        content = aTimelineItemStateEventContent(),
                        groupPosition = TimelineItemGroupPosition.None
                    ),
                    // 1 item with isMine = true
                    aTimelineItemEvent(
                        isMine = true,
                        content = content,
                        groupPosition = TimelineItemGroupPosition.None
                    ),
                    // A grouped event on top of it
                    aGroupedEvents(),
                    // A day separator
                    aTimelineItemDaySeparator(),
                ),
                // Render a focused event for an event with sender information displayed
                focusedEventIndex = 2,
            )
        ),
        onBackClick = {},
        onRoomDetailsClick = {},
        onEventContentClick = { _, _ -> false },
        onGalleryEventItemClick = { _, _, _ -> false },
        onUserDataClick = {},
        onLinkClick = { _, _ -> },
        onSendLocationClick = {},
        onCreatePollClick = {},
        onJoinCallClick = {},
        onViewAllPinnedMessagesClick = {},
        onThreadsListClick = {},
        forceJumpToBottomVisibility = true,
        knockRequestsBannerView = {},
        customReactionBottomSheet = {},
    )
}

