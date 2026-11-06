/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dev.zacsweers.metro.Inject
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.annotations.SessionCoroutineScope
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@Inject
class MediaSettingsPresenter(
    private val sessionPreferencesStore: SessionPreferencesStore,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val featureFlagService: FeatureFlagService,
) : Presenter<MediaSettingsState> {
    @Composable
    override fun present(): MediaSettingsState {
        val mediaOptimizationState by produceState<MediaOptimizationState?>(null) {
            val hasSplitMediaQualityOptionsFlow = featureFlagService.isFeatureEnabledFlow(FeatureFlags.SelectableMediaQuality)
            combine(
                hasSplitMediaQualityOptionsFlow,
                sessionPreferencesStore.doesOptimizeImages(),
                sessionPreferencesStore.getVideoCompressionPreset()
            ) { hasSplitOptions, compressImages, videoPreset ->
                if (hasSplitOptions) {
                    MediaOptimizationState.Split(
                        compressImages = compressImages,
                        videoPreset = videoPreset,
                    )
                } else {
                    MediaOptimizationState.AllMedia(isEnabled = compressImages)
                }
            }.collect { value = it }
        }

        fun handleEvent(event: MediaSettingsEvent) {
            when (event) {
                is MediaSettingsEvent.SetCompressMedia -> sessionCoroutineScope.launch {
                    sessionPreferencesStore.setOptimizeImages(event.compress)
                }
                is MediaSettingsEvent.SetVideoUploadQuality -> sessionCoroutineScope.launch {
                    sessionPreferencesStore.setVideoCompressionPreset(event.videoPreset)
                }
            }
        }

        return MediaSettingsState(
            mediaOptimizationState = mediaOptimizationState,
            eventSink = ::handleEvent,
        )
    }
}

