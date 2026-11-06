/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaupload.impl

import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.di.SessionScope
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.mediaupload.api.MediaOptimizationConfig
import com.echo.libraries.mediaupload.api.MediaOptimizationConfigProvider
import com.echo.libraries.preferences.api.store.SessionPreferencesStore
import com.echo.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.coroutines.flow.first

@ContributesBinding(SessionScope::class)
class DefaultMediaOptimizationConfigProvider(
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val featureFlagsService: FeatureFlagService,
) : MediaOptimizationConfigProvider {
    override suspend fun get(): MediaOptimizationConfig {
        val compressImages = sessionPreferencesStore.doesOptimizeImages().first()
        return MediaOptimizationConfig(
            compressImages = compressImages,
            videoCompressionPreset = if (featureFlagsService.isFeatureEnabled(FeatureFlags.SelectableMediaQuality)) {
                sessionPreferencesStore.getVideoCompressionPreset().first()
            } else {
                if (compressImages) VideoCompressionPreset.STANDARD else VideoCompressionPreset.HIGH
            },
        )
    }
}

