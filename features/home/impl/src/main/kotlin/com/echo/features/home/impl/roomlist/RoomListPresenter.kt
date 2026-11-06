/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl.roomlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.plan.Interaction
import com.echo.features.announcement.api.Announcement
import com.echo.features.announcement.api.AnnouncementService
import com.echo.features.home.impl.datasource.RoomListDataSource
import com.echo.features.home.impl.filters.RoomListFiltersState
import com.echo.features.home.impl.filters.into
import com.echo.features.home.impl.search.GlobalSearchState
import com.echo.features.home.impl.search.RoomListSearchEvent
import com.echo.features.home.impl.search.RoomListSearchState
import com.echo.features.home.impl.spacefilters.SpaceFiltersState
import com.echo.features.home.impl.spacefilters.into
import com.echo.features.home.impl.spacefilters.selectedFilter
import com.echo.features.invite.api.SeenInvitesStore
import com.echo.features.invite.api.acceptdecline.AcceptDeclineInviteEvent.AcceptInvite
import com.echo.features.invite.api.acceptdecline.AcceptDeclineInviteEvent.DeclineInvite
import com.echo.features.invite.api.acceptdecline.AcceptDeclineInviteState
import com.echo.features.leaveroom.api.LeaveRoomEvent
import com.echo.features.leaveroom.api.LeaveRoomState
import com.echo.features.preferences.impl.tasks.MarkRoomAsRead
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.featureflag.api.ShowAllActivityInRoomListFeature
import com.echo.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.encryption.RecoveryState
import com.echo.libraries.matrix.api.roomlist.RoomList
import com.echo.libraries.matrix.api.roomlist.RoomListFilter
import com.echo.libraries.matrix.ui.safety.rememberHideInvitesAvatar
import com.echo.libraries.push.api.battery.BatteryOptimizationState
import com.echo.libraries.sessionstorage.api.SessionStore
import com.echo.services.analytics.api.AnalyticsService
import com.echo.services.analytics.api.watchers.AnalyticsColdStartWatcher
import com.echo.services.analyticsproviders.api.trackers.captureInteraction
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

@Inject
class RoomListPresenter(
    private val client: MatrixClient,
    private val leaveRoomPresenter: Presenter<LeaveRoomState>,
    private val roomListDataSource: RoomListDataSource,
    private val filtersPresenter: Presenter<RoomListFiltersState>,
    private val searchPresenter: Presenter<RoomListSearchState>,
    private val analyticsService: AnalyticsService,
    private val acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState>,
    private val fullScreenIntentPermissionsPresenter: Presenter<FullScreenIntentPermissionsState>,
    private val batteryOptimizationPresenter: Presenter<BatteryOptimizationState>,
    private val markRoomAsRead: MarkRoomAsRead,
    private val seenInvitesStore: SeenInvitesStore,
    private val announcementService: AnnouncementService,
    private val coldStartWatcher: AnalyticsColdStartWatcher,
    private val spaceFiltersPresenter: Presenter<SpaceFiltersState>,
    private val globalSearchPresenter: Presenter<GlobalSearchState>,
    private val featureFlagService: FeatureFlagService,
    private val sessionStore: SessionStore,
) : Presenter<RoomListState> {
    private val encryptionService = client.encryptionService

    @Composable
    override fun present(): RoomListState {
        val coroutineScope = rememberCoroutineScope()
        val leaveRoomState = leaveRoomPresenter.present()
        val filtersState = filtersPresenter.present()
        val searchState = searchPresenter.present()
        val spaceFiltersState = spaceFiltersPresenter.present()
        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()
        val globalSearchState = globalSearchPresenter.present()

        LaunchedEffect(Unit) {
            roomListDataSource.launchIn(this)
        }

        var securityBannerDismissed by rememberSaveable { mutableStateOf(false) }
        val showNewNotificationSoundBanner by remember {
            announcementService.announcementsToShowFlow().map { announcements ->
                announcements.contains(Announcement.NewNotificationSound)
            }
        }.collectAsState(false)
        val showSwipeAvatarBanner by remember {
            combine(
                announcementService.announcementsToShowFlow(),
                sessionStore.sessionsFlow(),
            ) { announcements, sessions ->
                // The swipe gesture on the avatar is only available when there are several accounts
                sessions.size >= 2 && announcements.contains(Announcement.SwipeAvatarToSwitchAccount)
            }
        }.collectAsState(false)

        // Avatar indicator
        val hideInvitesAvatar by client.rememberHideInvitesAvatar()

        val contextMenu = remember { mutableStateOf<RoomListState.ContextMenu>(RoomListState.ContextMenu.Hidden) }
        val declineInviteMenu = remember { mutableStateOf<RoomListState.DeclineInviteMenu>(RoomListState.DeclineInviteMenu.Hidden) }

        val showAllActivity by produceState(initialValue = false) {
            featureFlagService.isFeatureEnabledFlow(ShowAllActivityInRoomListFeature).collect { value = it }
        }

        fun handleEvent(event: RoomListEvent) {
            when (event) {
                is RoomListEvent.UpdateVisibleRange -> coroutineScope.launch {
                    roomListDataSource.updateVisibleRange(event.range)
                }
                RoomListEvent.DismissRequestVerificationPrompt -> securityBannerDismissed = true
                RoomListEvent.DismissBanner -> securityBannerDismissed = true
                RoomListEvent.DismissNewNotificationSoundBanner -> coroutineScope.launch {
                    announcementService.onAnnouncementDismissed(Announcement.NewNotificationSound)
                }
                RoomListEvent.DismissSwipeAvatarBanner -> coroutineScope.launch {
                    announcementService.onAnnouncementDismissed(Announcement.SwipeAvatarToSwitchAccount)
                }
                RoomListEvent.ToggleSearchResults -> searchState.eventSink(RoomListSearchEvent.ToggleSearchVisibility)
                is RoomListEvent.ShowContextMenu -> {
                    coroutineScope.showContextMenu(event, contextMenu)
                }
                is RoomListEvent.HideContextMenu -> {
                    contextMenu.value = RoomListState.ContextMenu.Hidden
                }
                is RoomListEvent.LeaveRoom -> {
                    leaveRoomState.eventSink(LeaveRoomEvent.LeaveRoom(event.roomId, needsConfirmation = event.needsConfirmation))
                }
                is RoomListEvent.SetRoomIsFavorite -> coroutineScope.setRoomIsFavorite(event.roomId, event.isFavorite)
                is RoomListEvent.MarkAsRead -> coroutineScope.markAsRead(event.roomId)
                is RoomListEvent.MarkAsUnread -> coroutineScope.markAsUnread(event.roomId)
                is RoomListEvent.AcceptInvite -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptInvite(event.roomSummary.toInviteData())
                    )
                }
                is RoomListEvent.DeclineInvite -> {
                    acceptDeclineInviteState.eventSink(
                        DeclineInvite(event.roomSummary.toInviteData(), blockUser = event.blockUser, shouldConfirm = false)
                    )
                }
                is RoomListEvent.ShowDeclineInviteMenu -> declineInviteMenu.value = RoomListState.DeclineInviteMenu.Shown(event.roomSummary)
                RoomListEvent.HideDeclineInviteMenu -> declineInviteMenu.value = RoomListState.DeclineInviteMenu.Hidden
            }
        }

        LaunchedEffect(filtersState.filterSelectionStates, spaceFiltersState.selectedFilter()) {
            val selectedFilters = filtersState.selectedFilters().map { filter -> filter.into() }
            val selectedSpaceFilter = spaceFiltersState.selectedFilter().into()
            val allFilters = RoomListFilter.All(selectedFilters + listOfNotNull(selectedSpaceFilter))
            roomListDataSource.updateFilter(allFilters)
        }

        val canReportRoom by produceState(false) { value = client.canReportRoom() }
        val showUnreadCount by produceState(false) {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.UnreadIndicatorCount).collect { value = it }
        }

        val contentState = roomListContentState(
            securityBannerDismissed,
            showNewNotificationSoundBanner,
            showSwipeAvatarBanner,
            showUnreadCount,
            showAllActivity,
        )

        return RoomListState(
            contextMenu = contextMenu.value,
            declineInviteMenu = declineInviteMenu.value,
            leaveRoomState = leaveRoomState,
            filtersState = filtersState,
            searchState = searchState,
            globalSearchState = globalSearchState,
            spaceFiltersState = spaceFiltersState,
            contentState = contentState,
            acceptDeclineInviteState = acceptDeclineInviteState,
            hideInvitesAvatars = hideInvitesAvatar,
            canReportRoom = canReportRoom,
            eventSink = ::handleEvent,
        )
    }

    @Composable
    private fun rememberSecurityBannerState(
        securityBannerDismissed: Boolean,
    ): State<SecurityBannerState> {
        val currentSecurityBannerDismissed by rememberUpdatedState(securityBannerDismissed)
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()
        return remember {
            derivedStateOf {
                calculateBannerState(
                    securityBannerDismissed = currentSecurityBannerDismissed,
                    recoveryState = recoveryState,
                )
            }
        }
    }

    private fun calculateBannerState(
        securityBannerDismissed: Boolean,
        recoveryState: RecoveryState,
    ): SecurityBannerState {
        if (securityBannerDismissed) {
            return SecurityBannerState.None
        }

        when (recoveryState) {
            RecoveryState.DISABLED -> return SecurityBannerState.SetUpRecovery
            RecoveryState.INCOMPLETE -> return SecurityBannerState.RecoveryKeyConfirmation
            RecoveryState.UNKNOWN,
            RecoveryState.WAITING_FOR_SYNC,
            RecoveryState.ENABLED -> Unit
        }

        return SecurityBannerState.None
    }

    @Composable
    private fun roomListContentState(
        securityBannerDismissed: Boolean,
        showNewNotificationSoundBanner: Boolean,
        showSwipeAvatarBanner: Boolean,
        showUnreadCount: Boolean,
        showAllActivity: Boolean,
    ): RoomListContentState {
        val roomSummaries by produceState(initialValue = AsyncData.Loading()) {
            roomListDataSource.roomSummariesFlow.collect { value = AsyncData.Success(it) }
        }
        val loadingState by roomListDataSource.loadingState.collectAsState()
        val showEmpty by remember {
            derivedStateOf {
                (loadingState as? RoomList.LoadingState.Loaded)?.numberOfRooms == 0
            }
        }
        val showSkeleton by remember {
            derivedStateOf {
                loadingState == RoomList.LoadingState.NotLoaded || roomSummaries is AsyncData.Loading
            }
        }
        val seenRoomInvites by remember { seenInvitesStore.seenRoomIds() }.collectAsState(emptySet())
        val securityBannerState by rememberSecurityBannerState(securityBannerDismissed)
        return when {
            showEmpty -> RoomListContentState.Empty(
                securityBannerState = securityBannerState,
            )
            showSkeleton -> RoomListContentState.Skeleton(count = 16)
            else -> {
                coldStartWatcher.onRoomListVisible()

                RoomListContentState.Rooms(
                    securityBannerState = securityBannerState,
                    showNewNotificationSoundBanner = showNewNotificationSoundBanner,
                    showSwipeAvatarBanner = showSwipeAvatarBanner,
                    showUnreadCount = showUnreadCount,
                    fullScreenIntentPermissionsState = fullScreenIntentPermissionsPresenter.present(),
                    batteryOptimizationState = batteryOptimizationPresenter.present(),
                    summaries = roomSummaries.dataOrNull().orEmpty().toImmutableList(),
                    showAllActivity = showAllActivity,
                    seenRoomInvites = seenRoomInvites.toImmutableSet(),
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.showContextMenu(event: RoomListEvent.ShowContextMenu, contextMenuState: MutableState<RoomListState.ContextMenu>) = launch {
        val initialState = RoomListState.ContextMenu.Shown(
            roomId = event.roomSummary.roomId,
            roomName = event.roomSummary.name,
            isDm = event.roomSummary.isDm,
            isFavorite = event.roomSummary.isFavorite,
            hasNewContent = event.roomSummary.hasNewContent,
        )
        contextMenuState.value = initialState

        client.getRoom(event.roomSummary.roomId)?.use { room ->

            val isShowingContextMenuFlow = snapshotFlow { contextMenuState.value is RoomListState.ContextMenu.Shown }
                .distinctUntilChanged()

            val isFavoriteFlow = room.roomInfoFlow
                .map { it.isFavorite }
                .distinctUntilChanged()

            isFavoriteFlow
                .onEach { isFavorite ->
                    contextMenuState.value = initialState.copy(isFavorite = isFavorite)
                }
                .flatMapLatest { isShowingContextMenuFlow }
                .takeWhile { isShowingContextMenu -> isShowingContextMenu }
                .collect()
        }
    }

    private fun CoroutineScope.setRoomIsFavorite(roomId: RoomId, isFavorite: Boolean) = launch {
        client.getRoom(roomId)?.use { room ->
            room.setIsFavorite(isFavorite)
                .onSuccess {
                    analyticsService.captureInteraction(name = Interaction.Name.MobileRoomListRoomContextMenuFavouriteToggle)
                }
        }
    }

    private fun CoroutineScope.markAsRead(roomId: RoomId) = launch {
        markRoomAsRead(roomId)
            .onSuccess {
                analyticsService.captureInteraction(name = Interaction.Name.MobileRoomListRoomContextMenuUnreadToggle)
            }
    }

    private fun CoroutineScope.markAsUnread(roomId: RoomId) = launch {
        client.getRoom(roomId)?.use { room ->
            room.setUnreadFlag(isUnread = true)
                .onSuccess {
                    analyticsService.captureInteraction(name = Interaction.Name.MobileRoomListRoomContextMenuUnreadToggle)
                }
        }
    }
}

