/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.test.timeline.voicemessages.composer

import com.echo.features.messages.impl.voicemessages.composer.DefaultVoiceMessageComposerPresenter
import com.echo.features.messages.impl.voicemessages.composer.VoiceMessageComposerPlayer
import com.echo.features.messages.test.FakeMessageComposerContext
import com.echo.libraries.audio.test.FakeAudioFocus
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.mediaplayer.test.FakeMediaPlayer
import com.echo.libraries.mediaupload.api.MediaSender
import com.echo.libraries.mediaupload.test.FakeMediaSender
import com.echo.libraries.permissions.test.FakePermissionsPresenterFactory
import com.echo.libraries.voicerecorder.test.FakeVoiceRecorder
import com.echo.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.CoroutineScope

class FakeDefaultVoiceMessageComposerPresenterFactory(
    private val sessionCoroutineScope: CoroutineScope,
    private val mediaSender: MediaSender = FakeMediaSender(),
) : DefaultVoiceMessageComposerPresenter.Factory {
    override fun create(timelineMode: Timeline.Mode): DefaultVoiceMessageComposerPresenter {
        return DefaultVoiceMessageComposerPresenter(
            sessionCoroutineScope = sessionCoroutineScope,
            timelineMode = timelineMode,
            voiceRecorder = FakeVoiceRecorder(),
            analyticsService = FakeAnalyticsService(),
            audioFocus = FakeAudioFocus(
                requestAudioFocusResult = { _, _ -> },
                releaseAudioFocusResult = { },
            ),
            mediaSenderFactory = { mediaSender },
            player = VoiceMessageComposerPlayer(
                mediaPlayer = FakeMediaPlayer(),
                sessionCoroutineScope = sessionCoroutineScope,
            ),
            messageComposerContext = FakeMessageComposerContext(),
            permissionsPresenterFactory = FakePermissionsPresenterFactory(),
        )
    }
}

