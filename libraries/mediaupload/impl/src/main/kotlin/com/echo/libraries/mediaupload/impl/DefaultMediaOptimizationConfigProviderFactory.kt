/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaupload.impl

import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.mediaupload.api.MediaOptimizationConfigProvider
import com.echo.libraries.mediaupload.api.MediaOptimizationConfigProviderFactory
import com.echo.libraries.preferences.api.store.SessionPreferencesStoreFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class DefaultMediaOptimizationConfigProviderFactory(
    private val sessionPreferencesStoreFactory: SessionPreferencesStoreFactory,
    private val featureFlagService: FeatureFlagService,
) : MediaOptimizationConfigProviderFactory {
    override fun create(client: MatrixClient): MediaOptimizationConfigProvider {
        return DefaultMediaOptimizationConfigProvider(
            sessionPreferencesStore = sessionPreferencesStoreFactory.get(client.sessionId, client.sessionCoroutineScope),
            featureFlagsService = featureFlagService,
        )
    }
}
