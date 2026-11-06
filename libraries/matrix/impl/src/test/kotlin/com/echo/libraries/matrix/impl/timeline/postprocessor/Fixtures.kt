/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.timeline.postprocessor

import com.echo.libraries.matrix.api.core.UniqueId
import com.echo.libraries.matrix.api.timeline.MatrixTimelineItem
import com.echo.libraries.matrix.api.timeline.item.event.MembershipChange
import com.echo.libraries.matrix.api.timeline.item.event.OtherState
import com.echo.libraries.matrix.api.timeline.item.event.StateContent
import com.echo.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.A_USER_ID_2
import com.echo.libraries.matrix.test.timeline.aMessageContent
import com.echo.libraries.matrix.test.timeline.aProfileChangeMessageContent
import com.echo.libraries.matrix.test.timeline.anEventTimelineItem
import com.echo.libraries.matrix.test.timeline.item.event.aRoomMembershipContent

internal val timelineStartEvent = MatrixTimelineItem.Virtual(
    uniqueId = UniqueId("timeline_start"),
    virtual = VirtualTimelineItem.RoomBeginning,
)
internal val roomCreateEvent = MatrixTimelineItem.Event(
    uniqueId = UniqueId("m.room.create"),
    event = anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))
)
internal val roomCreatorJoinEvent = MatrixTimelineItem.Event(
    uniqueId = UniqueId("m.room.member"),
    event = anEventTimelineItem(content = aRoomMembershipContent(userId = A_USER_ID, change = MembershipChange.JOINED))
)
internal val otherMemberJoinEvent = MatrixTimelineItem.Event(
    uniqueId = UniqueId("m.room.member_other"),
    event = anEventTimelineItem(content = aRoomMembershipContent(userId = A_USER_ID_2, change = MembershipChange.JOINED))
)
internal val otherMemberLeaveEvent = MatrixTimelineItem.Event(
    uniqueId = UniqueId("m.room.member_leave"),
    event = anEventTimelineItem(content = aRoomMembershipContent(userId = A_USER_ID_2, change = MembershipChange.LEFT))
)
internal val profileChangeEvent = MatrixTimelineItem.Event(
    uniqueId = UniqueId("m.room.member_profile"),
    event = anEventTimelineItem(content = aProfileChangeMessageContent(displayName = "New Name", prevDisplayName = "Old Name"))
)
internal val messageEvent = MatrixTimelineItem.Event(
    uniqueId = UniqueId("m.room.message"),
    event = anEventTimelineItem(content = aMessageContent("hi"))
)
internal val messageEvent2 = MatrixTimelineItem.Event(
    uniqueId = UniqueId("m.room.message2"),
    event = anEventTimelineItem(content = aMessageContent("hello"))
)
internal val dayEvent = MatrixTimelineItem.Virtual(
    uniqueId = UniqueId("day"),
    virtual = VirtualTimelineItem.DayDivider(0),
)

