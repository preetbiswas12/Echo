/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.attachments.video

import dev.zacsweers.metro.Inject
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.collections.immutable.ImmutableList

@Inject
class VideoCompressionPresetSelector {
    fun selectBestVideoPreset(
        expectedVideoPreset: VideoCompressionPreset,
        videoSizeEstimations: AsyncData<ImmutableList<VideoUploadEstimation>>,
    ): AsyncData<VideoCompressionPreset> {
        val estimations = videoSizeEstimations.dataOrNull() ?: return AsyncData.Loading()
        val bestEstimation = estimations.find { it.preset.ordinal >= expectedVideoPreset.ordinal && it.canUpload }?.preset
        return if (bestEstimation != null) {
            AsyncData.Success(bestEstimation)
        } else {
            AsyncData.Failure(
                IllegalStateException("No suitable video preset found for expected preset: $expectedVideoPreset")
            )
        }
    }
}

