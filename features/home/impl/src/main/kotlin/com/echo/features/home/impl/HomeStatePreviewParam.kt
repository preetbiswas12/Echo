/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.features.home.impl.roomlist.RoomListState
import com.echo.features.home.impl.roomlist.RoomListStatePreviewParam
import com.echo.features.home.impl.roomlist.aRoomListState
import com.echo.features.home.impl.roomlist.aRoomsContentState
import com.echo.features.home.impl.roomlist.generateRoomListRoomSummaryList
import com.echo.features.home.impl.spaces.HomeSpacesState
import com.echo.features.home.impl.spaces.aHomeSpacesState
import com.echo.libraries.designsystem.utils.snackbar.SnackbarMessage
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

open class HomeStatePreviewParam : PreviewParameterProvider<HomeState> {
    override val values: Sequence<HomeState>
        get() = sequenceOf(
            aHomeState(),
            aHomeState(hasNetworkConnection = false),
            aHomeState(snackbarMessage = SnackbarMessage(CommonStrings.common_verification_complete)),
            aHomeState(
                roomListState = aRoomListState(
                    // Add more rooms to see the blur effect under the NavigationBar
                    contentState = aRoomsContentState(
                        summaries = generateRoomListRoomSummaryList(),
                    )
                ),
                // For the bottom nav bar to be visible in the preview, the user must be member of at least one space
                homeSpacesState = aHomeSpacesState(),
            ),
            aHomeState(
                currentHomeNavigationBarItem = HomeNavigationBarItem.Spaces,
            ),
        ) + RoomListStatePreviewParam().values.map {
            aHomeState(roomListState = it)
        } + aHomeState(
            currentHomeNavigationBarItem = HomeNavigationBarItem.Spaces,
            homeSpacesState = aHomeSpacesState(
                spaceRooms = emptyList(),
            ),
        ) + aHomeState(
            showMultiAccountAnnouncement = true,
        )
}

internal fun aHomeState(
    matrixUser: MatrixUser = MatrixUser(userId = UserId("@id:domain"), displayName = "User#1"),
    currentUserAndNeighbors: List<MatrixUser> = listOf(matrixUser),
    showAvatarIndicator: Boolean = false,
    hasNetworkConnection: Boolean = true,
    snackbarMessage: SnackbarMessage? = null,
    currentHomeNavigationBarItem: HomeNavigationBarItem = HomeNavigationBarItem.Chats,
    roomListState: RoomListState = aRoomListState(),
    homeSpacesState: HomeSpacesState = aHomeSpacesState(),
    canReportBug: Boolean = true,
    showMultiAccountAnnouncement: Boolean = false,
    totalUnreadCount: Long = 0L,
    eventSink: (HomeEvent) -> Unit = {}
) = HomeState(
    currentUserAndNeighbors = currentUserAndNeighbors.toImmutableList(),
    showAvatarIndicator = showAvatarIndicator,
    hasNetworkConnection = hasNetworkConnection,
    snackbarMessage = snackbarMessage,
    canReportBug = canReportBug,
    showMultiAccountAnnouncement = showMultiAccountAnnouncement,
    currentHomeNavigationBarItem = currentHomeNavigationBarItem,
    roomListState = roomListState,
    homeSpacesState = homeSpacesState,
    totalUnreadCount = totalUnreadCount,
    eventSink = eventSink,
)
