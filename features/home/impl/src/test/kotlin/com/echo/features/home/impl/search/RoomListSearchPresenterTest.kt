/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl.search

import com.google.common.truth.Truth.assertThat
import com.echo.features.home.impl.datasource.aRoomListRoomSummaryFactory
import com.echo.libraries.dateformatter.test.FakeDateFormatter
import com.echo.libraries.eventformatter.test.FakeRoomLatestEventFormatter
import com.echo.libraries.matrix.api.roomlist.RoomListFilter
import com.echo.libraries.matrix.api.roomlist.RoomListService
import com.echo.libraries.matrix.test.room.aRoomSummary
import com.echo.libraries.matrix.test.roomlist.FakeDynamicRoomList
import com.echo.libraries.matrix.test.roomlist.FakeRoomListService
import com.echo.tests.testutils.lambda.assert
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.test
import com.echo.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomListSearchPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRoomListSearchPresenter()
        presenter.test {
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isFalse()
                assertThat(state.query.text.toString()).isEmpty()
                assertThat(state.results).isEmpty()
            }
        }
    }

    @Test
    fun `present - toggle search visibility`() = runTest {
        val presenter = createRoomListSearchPresenter()
        presenter.test {
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isFalse()
                state.eventSink(RoomListSearchEvent.ToggleSearchVisibility)
            }
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isTrue()
                state.eventSink(RoomListSearchEvent.ToggleSearchVisibility)
            }
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isFalse()
            }
        }
    }

    @Test
    fun `present - query search changes`() = runTest {
        val roomList = FakeDynamicRoomList()
        val roomListService = FakeRoomListService(
            createRoomListLambda = { roomList }
        )
        val presenter = createRoomListSearchPresenter(roomListService)
        presenter.test {
            awaitItem().let { state ->
                assertThat(
                    roomList.currentFilter.value
                ).isEqualTo(
                    RoomListFilter.None
                )
                state.query.edit { append("Search") }
            }
            awaitItem().let { state ->
                assertThat(state.query.text).isEqualTo("Search")
                assertThat(
                    roomList.currentFilter.value
                ).isEqualTo(
                    RoomListFilter.NormalizedMatchRoomName("Search")
                )
                state.eventSink(RoomListSearchEvent.ClearQuery)
            }
            awaitItem().let { state ->
                assertThat(state.query.text.toString()).isEmpty()
                assertThat(
                    roomList.currentFilter.value
                ).isEqualTo(
                    RoomListFilter.None
                )
            }
        }
    }

    @Test
    fun `present - room list changes`() = runTest {
        val roomList = FakeDynamicRoomList()
        val roomListService = FakeRoomListService(
            createRoomListLambda = { roomList }
        )
        val presenter = createRoomListSearchPresenter(roomListService)
        presenter.test {
            awaitItem().let { state ->
                assertThat(state.results).isEmpty()
            }
            roomList.summaries.emit(
                listOf(aRoomSummary())
            )
            awaitItem().let { state ->
                assertThat(state.results).hasSize(1)
            }
            roomList.summaries.emit(emptyList())
            awaitItem().let { state ->
                assertThat(state.results).isEmpty()
            }
        }
    }

    @Test
    fun `present - UpdateVisibleRange triggers pagination when near end`() = runTest {
        val loadMoreLambda = lambdaRecorder<Unit> { }
        val roomList = FakeDynamicRoomList(loadMoreLambda = loadMoreLambda)
        val roomListService = FakeRoomListService(
            createRoomListLambda = { roomList }
        )
        val presenter = createRoomListSearchPresenter(roomListService)
        presenter.test {
            val initialState = awaitItem()
            // Post some rooms to simulate loaded content
            val rooms = (1..10).map { aRoomSummary() }
            roomList.summaries.emit(rooms)
            skipItems(1)

            // UpdateVisibleRange near end should trigger loadMore
            initialState.eventSink(RoomListSearchEvent.UpdateVisibleRange(IntRange(0, 9)))
            // Give time for the coroutine to complete
            testScheduler.advanceUntilIdle()

            assert(loadMoreLambda).isCalledOnce()
        }
    }
}

fun TestScope.createRoomListSearchPresenter(
    roomListService: RoomListService = FakeRoomListService(),
): RoomListSearchPresenter {
    return RoomListSearchPresenter(
        dataSourceFactory = object : RoomListSearchDataSource.Factory {
            override fun create(coroutineScope: CoroutineScope): RoomListSearchDataSource {
                return RoomListSearchDataSource(
                    roomListService = roomListService,
                    roomSummaryFactory = aRoomListRoomSummaryFactory(
                        dateFormatter = FakeDateFormatter(),
                        roomLatestEventFormatter = FakeRoomLatestEventFormatter(),
                    ),
                    coroutineDispatchers = testCoroutineDispatchers(),
                    coroutineScope = coroutineScope,
                )
            }
        }
    )
}

