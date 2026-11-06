/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.roomlist

import com.echo.libraries.matrix.api.roomlist.DynamicRoomList
import com.echo.libraries.matrix.api.roomlist.RoomList
import com.echo.libraries.matrix.api.roomlist.RoomListFilter
import com.echo.libraries.matrix.api.roomlist.RoomListFilter.Companion.all
import com.echo.libraries.matrix.api.roomlist.RoomSummary
import com.echo.services.analytics.api.AnalyticsLongRunningTransaction
import com.echo.services.analytics.api.AnalyticsService
import com.echo.services.analytics.api.finishLongRunningTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.RoomListLoadingState
import org.matrix.rustcomponents.sdk.RoomListService
import kotlin.coroutines.CoroutineContext
import org.matrix.rustcomponents.sdk.RoomList as InnerRoomList

internal class RoomListFactory(
    private val innerRoomListService: RoomListService,
    private val analyticsService: AnalyticsService,
) {
    private val roomSummaryFactory: RoomSummaryFactory = RoomSummaryFactory()

    /**
     * Creates a room list that can be used to load more rooms and filter them dynamically.
     */
    fun createRoomList(
        pageSize: Int,
        coroutineContext: CoroutineContext,
        coroutineScope: CoroutineScope,
        initialFilter: RoomListFilter = all(),
        innerProvider: suspend () -> InnerRoomList
    ): DynamicRoomList {
        val loadingStateFlow: MutableStateFlow<RoomList.LoadingState> = MutableStateFlow(RoomList.LoadingState.NotLoaded)
        val summariesFlow = MutableSharedFlow<List<RoomSummary>>(replay = 1, extraBufferCapacity = 1)
        val processor = RoomSummaryListProcessor(summariesFlow, innerRoomListService, coroutineContext, roomSummaryFactory, analyticsService)
        var innerRoomList: InnerRoomList? = null
        val dynamicRoomList = RustDynamicRoomList(
            summaries = summariesFlow,
            loadingState = loadingStateFlow,
            processor = processor,
            pageSize = pageSize,
            initialFilter = initialFilter,
        )

        val firstRoomsTransaction = analyticsService.startTransaction("Load first set of rooms", "innerRoomList.entriesFlow")

        coroutineScope.launch(coroutineContext) {
            innerRoomList = innerProvider()
            innerRoomList.let { innerRoomList ->
                innerRoomList.entriesFlow(
                    pageSize = pageSize,
                    initialFilterKind = RoomListFilterMapper.toRustFilter(initialFilter),
                    onControllerCreated = { controller ->
                        dynamicRoomList.onControllerCreated(controller, initialFilter)
                    },
                    onControllerDestroyed = { controller ->
                        dynamicRoomList.onControllerDestroyed(controller)
                    },
                ).onEach { update ->
                    if (!firstRoomsTransaction.isFinished()) {
                        analyticsService.finishLongRunningTransaction(AnalyticsLongRunningTransaction.FirstRoomsDisplayed)
                        firstRoomsTransaction.finish()
                    }
                    processor.postUpdate(update)
                }.launchIn(this)

                innerRoomList.loadingStateFlow()
                    .map { it.toLoadingState() }
                    .onEach {
                        loadingStateFlow.value = it
                    }
                    .launchIn(this)
            }
        }.invokeOnCompletion {
            innerRoomList?.destroy()
        }
        return dynamicRoomList
    }
}

private fun RoomListLoadingState.toLoadingState(): RoomList.LoadingState {
    return when (this) {
        is RoomListLoadingState.Loaded -> RoomList.LoadingState.Loaded(maximumNumberOfRooms?.toInt() ?: 0)
        RoomListLoadingState.NotLoaded -> RoomList.LoadingState.NotLoaded
    }
}

