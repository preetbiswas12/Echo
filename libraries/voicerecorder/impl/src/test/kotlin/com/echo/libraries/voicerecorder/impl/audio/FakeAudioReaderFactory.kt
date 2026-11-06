/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.voicerecorder.impl.audio

import com.echo.libraries.core.coroutine.CoroutineDispatchers

class FakeAudioReaderFactory(
    private val audio: List<Audio>
) : AudioReader.Factory {
    var createdCount: Int = 0
        private set

    override fun create(config: AudioConfig, dispatchers: CoroutineDispatchers): AudioReader {
        createdCount++
        return FakeAudioReader(dispatchers, audio)
    }
}

