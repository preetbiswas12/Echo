/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.di

import com.echo.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import com.echo.features.messages.impl.timeline.model.event.ensureActiveLiveLocation
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.voiceplayer.api.VoiceMessageState
import com.echo.libraries.voiceplayer.api.aVoiceMessageState

/**
 * A fake [TimelineItemPresenterFactories] for screenshot tests.
 */
fun aFakeTimelineItemPresenterFactories() = TimelineItemPresenterFactories(
    mapOf(
        Pair(
            TimelineItemLocationContent::class,
            TimelineItemPresenterFactory<TimelineItemLocationContent, TimelineItemLocationContent> { content ->
                Presenter { content.ensureActiveLiveLocation() }
            },
        ),
        Pair(
            TimelineItemVoiceContent::class,
            TimelineItemPresenterFactory<TimelineItemVoiceContent, VoiceMessageState> { Presenter { aVoiceMessageState() } },
        ),
    )
)

