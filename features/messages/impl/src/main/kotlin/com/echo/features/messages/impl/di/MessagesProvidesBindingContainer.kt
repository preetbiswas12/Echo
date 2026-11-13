/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.di

import com.echo.features.messages.impl.timeline.TimelineController
import com.echo.features.messages.impl.timeline.di.LiveTimeline
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.di.RoomScope
import com.echo.libraries.di.annotations.RoomCoroutineScope
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.libraries.matrix.api.timeline.Timeline
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope

@ContributesTo(RoomScope::class)
@BindingContainer
object MessagesProvidesBindingContainer {
    @Provides
    @LiveTimeline
    fun providesLiveTimeline(joinedRoom: JoinedRoom): Timeline = joinedRoom.liveTimeline

    @Provides
    @SingleIn(RoomScope::class)
    fun providesTimelineController(
        joinedRoom: JoinedRoom,
        @LiveTimeline liveTimeline: Timeline,
        @RoomCoroutineScope roomCoroutineScope: CoroutineScope,
        dispatchers: CoroutineDispatchers,
    ): TimelineController = TimelineController(joinedRoom, liveTimeline, roomCoroutineScope, dispatchers)
}
