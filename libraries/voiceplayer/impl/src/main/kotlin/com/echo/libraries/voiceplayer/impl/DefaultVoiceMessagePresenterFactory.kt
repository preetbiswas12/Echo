/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.voiceplayer.impl

import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.RoomScope
import com.echo.libraries.di.annotations.SessionCoroutineScope
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.libraries.voiceplayer.api.VoiceMessagePresenterFactory
import com.echo.libraries.voiceplayer.api.VoiceMessageState
import com.echo.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration

@ContributesBinding(RoomScope::class)
class DefaultVoiceMessagePresenterFactory(
    private val analyticsService: AnalyticsService,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val voiceMessagePlayerFactory: VoiceMessagePlayer.Factory,
    private val voicePlayerStore: VoicePlayerStore,
) : VoiceMessagePresenterFactory {
    override fun createVoiceMessagePresenter(
        eventId: EventId?,
        mediaSource: MediaSource,
        mimeType: String?,
        filename: String?,
        duration: Duration,
    ): Presenter<VoiceMessageState> {
        val player = voiceMessagePlayerFactory.create(
            eventId = eventId,
            mediaSource = mediaSource,
            mimeType = mimeType,
            filename = filename,
        )

        return VoiceMessagePresenter(
            analyticsService = analyticsService,
            sessionCoroutineScope = sessionCoroutineScope,
            voicePlayerStore = voicePlayerStore,
            player = player,
            eventId = eventId,
            duration = duration,
        )
    }
}

