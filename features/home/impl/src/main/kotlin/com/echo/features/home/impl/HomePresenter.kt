/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.echo.features.announcement.api.Announcement
import com.echo.features.announcement.api.AnnouncementService
import com.echo.features.home.impl.roomlist.RoomListState
import com.echo.features.home.impl.spaces.HomeSpacesState
import com.echo.features.rageshake.api.RageshakeFeatureAvailability
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import com.echo.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import com.echo.libraries.di.annotations.AppCoroutineScope
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.indicator.api.IndicatorService
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.sync.SyncService
import com.echo.libraries.sessionstorage.api.SessionStore
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Inject
class HomePresenter(
    private val client: MatrixClient,
    private val syncService: SyncService,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val indicatorService: IndicatorService,
    private val roomListPresenter: Presenter<RoomListState>,
    private val homeSpacesPresenter: Presenter<HomeSpacesState>,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
    private val sessionStore: SessionStore,
    private val announcementService: AnnouncementService,
    private val featureFlagService: FeatureFlagService,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
) : Presenter<HomeState> {
    private val currentUserWithNeighborsBuilder = CurrentUserWithNeighborsBuilder()

    @Composable
    override fun present(): HomeState {
        val coroutineScope = rememberCoroutineScope()
        val matrixUser by client.userProfile.collectAsState()
        val totalUnreadCount by client.totalUnreadCountFlow.collectAsState()
        val currentUserAndNeighbors by remember {
            combine(
                client.userProfile,
                sessionStore.sessionsFlow(),
                currentUserWithNeighborsBuilder::build,
            )
        }.collectAsState(initial = persistentListOf(matrixUser))
        val isOnline by syncService.isOnline.collectAsState()
        val canReportBug by remember { rageshakeFeatureAvailability.isAvailable() }.collectAsState(false)
        val showMultiAccountAnnouncement by remember {
            combine(
                announcementService.announcementsToShowFlow(),
                featureFlagService.isFeatureEnabledFlow(FeatureFlags.MultiAccount),
                sessionStore.sessionsFlow(),
            ) { announcements, isMultiAccountEnabled, sessions ->
                // Do not announce the feature to users who already have several accounts
                isMultiAccountEnabled && sessions.size == 1 && announcements.contains(Announcement.MultiAccount)
            }
        }.collectAsState(false)
        LaunchedEffect(Unit) {
            // The user already uses several accounts, so the feature does not need to be announced anymore,
            // even if they later remove accounts.
            combine(
                announcementService.announcementsToShowFlow(),
                sessionStore.sessionsFlow(),
            ) { announcements, sessions ->
                announcements.contains(Announcement.MultiAccount) && sessions.size > 1
            }
                .filter { it }
                .collect {
                    announcementService.onAnnouncementDismissed(Announcement.MultiAccount)
                }
        }
        val roomListState = roomListPresenter.present()
        val homeSpacesState = homeSpacesPresenter.present()
        var currentHomeNavigationBarItemOrdinal by rememberSaveable { mutableIntStateOf(HomeNavigationBarItem.Chats.ordinal) }
        val currentHomeNavigationBarItem by remember {
            derivedStateOf {
                HomeNavigationBarItem.from(currentHomeNavigationBarItemOrdinal)
            }
        }
        LaunchedEffect(Unit) {
            // Force a refresh of the profile
            client.getUserProfile()
        }
        // Avatar indicator
        val showAvatarIndicator by indicatorService.showRoomListTopBarIndicator()

        fun handleEvent(event: HomeEvent) {
            when (event) {
                is HomeEvent.SelectHomeNavigationBarItem -> {
                    currentHomeNavigationBarItemOrdinal = event.item.ordinal
                }
                // The user is switching account, which destroys this presenter, so use the app scope to not be cancelled.
                HomeEvent.DismissSwipeAvatarAnnouncement -> appCoroutineScope.launch {
                    announcementService.onAnnouncementDismissed(Announcement.SwipeAvatarToSwitchAccount)
                }
                HomeEvent.DismissMultiAccountAnnouncement -> coroutineScope.launch {
                    announcementService.onAnnouncementDismissed(Announcement.MultiAccount)
                }
            }
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        return HomeState(
            currentUserAndNeighbors = currentUserAndNeighbors,
            showAvatarIndicator = showAvatarIndicator,
            hasNetworkConnection = isOnline,
            currentHomeNavigationBarItem = currentHomeNavigationBarItem,
            roomListState = roomListState,
            homeSpacesState = homeSpacesState,
            snackbarMessage = snackbarMessage,
            canReportBug = canReportBug,
            showMultiAccountAnnouncement = showMultiAccountAnnouncement,
            totalUnreadCount = totalUnreadCount,
            eventSink = ::handleEvent,
        )
    }
}
