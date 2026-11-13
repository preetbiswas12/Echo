/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.factories

import app.cash.turbine.test
import com.echo.features.messages.impl.fixtures.aTimelineItemsFactory
import com.echo.features.messages.impl.timeline.model.TimelineItem
import com.echo.features.messages.impl.timeline.model.TimelineItemGroupPosition
import com.echo.features.messages.impl.timeline.model.TimelineItemThreadInfo
import com.echo.features.messages.impl.timeline.model.event.TimelineItemTextContent
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.matrix.api.core.UniqueId
import com.echo.libraries.matrix.api.timeline.MatrixTimelineItem
import com.echo.libraries.matrix.api.timeline.item.EventThreadInfo
import com.echo.libraries.matrix.api.timeline.item.ThreadSummary
import com.echo.libraries.matrix.api.timeline.item.event.OtherMessageType
import com.echo.libraries.matrix.api.timeline.item.event.ProfileDetails
import com.echo.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import com.echo.libraries.matrix.test.A_THREAD_ID
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.timeline.aMessageContent
import com.echo.libraries.matrix.test.timeline.aRedactedContent
import com.echo.libraries.matrix.test.timeline.anEventTimelineItem
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TimelineItemsFactoryTest {
    @Test
    fun `messages sent close together are grouped`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, ONE_MINUTE, 2 * ONE_MINUTE),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Middle,
            TimelineItemGroupPosition.Last,
        ).inOrder()
    }

    @Test
    fun `messages sent more than five minutes apart are not grouped`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, 6 * ONE_MINUTE, 12 * ONE_MINUTE),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.None,
            TimelineItemGroupPosition.None,
            TimelineItemGroupPosition.None,
        ).inOrder()
    }

    @Test
    fun `a gap of more than five minutes splits the group`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, ONE_MINUTE, 8 * ONE_MINUTE, 9 * ONE_MINUTE),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Last,
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Last,
        ).inOrder()
    }

    @Test
    fun `a gap of exactly five minutes still groups`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, FIVE_MINUTES),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Last,
        ).inOrder()
    }

    @Test
    fun `messages with the same timestamp are grouped`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, 0L, 0L),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Middle,
            TimelineItemGroupPosition.Last,
        ).inOrder()
    }

    @Test
    fun `redacted message keeps its thread info`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    timestamp = 0L,
                    content = aRedactedContent(
                        threadInfo = EventThreadInfo.ThreadResponse(A_THREAD_ID),
                    ),
                ),
            )
        )
        var threadInfo: TimelineItemThreadInfo? = null
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
            )
            threadInfo = awaitItem()
                .filterIsInstance<TimelineItem.Event>()
                .firstOrNull()
                ?.threadInfo
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(threadInfo).isEqualTo(TimelineItemThreadInfo.ThreadResponse(A_THREAD_ID))
    }

    @Test
    fun `a key verification request is not rendered`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    content = aMessageContent(
                        body = "Alice is requesting to verify your key, but your client does not support in-chat key verification.",
                        messageType = OtherMessageType(
                            msgType = "m.key.verification.request",
                            body = "Alice is requesting to verify your key, but your client does not support in-chat key verification.",
                        ),
                    ),
                ),
            ),
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-1"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    content = aMessageContent(body = "A regular message"),
                ),
            ),
        )
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
            )
            val bodies = awaitItem()
                .filterIsInstance<TimelineItem.Event>()
                .map { (it.content as TimelineItemTextContent).body }
            assertThat(bodies).containsExactly("A regular message")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a deleted message keeps the summary of the thread it heads`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    content = aRedactedContent(
                        threadInfo = EventThreadInfo.ThreadRoot(
                            summary = ThreadSummary(latestEvent = AsyncData.Uninitialized, numberOfReplies = 3),
                        ),
                    ),
                ),
            ),
        )
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
            )
            val threadInfo = awaitItem()
                .filterIsInstance<TimelineItem.Event>()
                .single()
                .threadInfo
            assertThat(threadInfo).isInstanceOf(TimelineItemThreadInfo.ThreadRoot::class.java)
            assertThat((threadInfo as TimelineItemThreadInfo.ThreadRoot).summary.numberOfReplies).isEqualTo(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a sender the room member list knows nothing about keeps the profile the timeline gave`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    senderProfile = ProfileDetails.Unavailable,
                    content = aMessageContent(body = "A regular message"),
                ),
            ),
        )
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
            )
            val event = awaitItem().filterIsInstance<TimelineItem.Event>().single()
            assertThat(event.senderProfile).isEqualTo(ProfileDetails.Unavailable)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a pass with the same inputs reuses the cached items`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(computeReadReceipts = true, computeReactions = true),
        )
        val items = List(5) { index ->
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-$index"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    senderProfile = ProfileDetails.Unavailable,
                    content = aMessageContent(body = "Message $index"),
                ),
            )
        }

        factory.replaceWith(timelineItems = items)
        val first = factory.timelineItems.first().filterIsInstance<TimelineItem.Event>()

        factory.replaceWith(timelineItems = items)
        val second = factory.timelineItems.first().filterIsInstance<TimelineItem.Event>()
        assertThat(first).containsExactlyElementsIn(second)
    }

    @Test
    fun `an unchanged virtual item does not invalidate the events around it`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(computeReadReceipts = true, computeReactions = true),
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(sender = A_USER_ID, content = aMessageContent(body = "Message 0")),
            ),
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("day-divider"),
                virtual = VirtualTimelineItem.DayDivider(timestamp = 0L),
            ),
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-1"),
                event = anEventTimelineItem(sender = A_USER_ID, content = aMessageContent(body = "Message 1")),
            ),
        )

        factory.replaceWith(timelineItems = items)
        val first = factory.timelineItems.first().filterIsInstance<TimelineItem.Event>()

        factory.replaceWith(timelineItems = items)
        val second = factory.timelineItems.first().filterIsInstance<TimelineItem.Event>()

        assertThat(second).hasSize(first.size)
        second.forEachIndexed { index, item ->
            assertThat(item).isSameInstanceAs(first[index])
        }
    }

    private suspend fun TestScope.groupPositionsOf(timestamps: List<Long>): List<TimelineItemGroupPosition> {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = timestamps.mapIndexed { index, timestamp ->
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-$index"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    timestamp = timestamp,
                    content = aMessageContent(body = "Message $index"),
                ),
            )
        }
        var positions: List<TimelineItemGroupPosition> = emptyList()
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
            )
            positions = awaitItem()
                .filterIsInstance<TimelineItem.Event>()
                .map { it.groupPosition }
                .reversed()
            cancelAndIgnoreRemainingEvents()
        }
        return positions
    }

    private companion object {
        const val ONE_MINUTE = 60 * 1000L
        const val FIVE_MINUTES = 5 * ONE_MINUTE
    }
}
