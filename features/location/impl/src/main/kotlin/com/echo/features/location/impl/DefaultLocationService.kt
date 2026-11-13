/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.location.impl

import com.echo.features.enterprise.api.remoteconfig.CustomMapTilerConfigProvider
import com.echo.features.enterprise.api.remoteconfig.MapTilerConfig
import com.echo.features.location.api.LocationService
import com.echo.features.location.api.buildTimeMapTilerConfig
import com.echo.libraries.di.SessionScope
import com.echo.libraries.di.annotations.SessionCoroutineScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@SingleIn(SessionScope::class)
@ContributesBinding(SessionScope::class)
class DefaultLocationService(
    customMapTilerConfigProvider: CustomMapTilerConfigProvider,
    @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
) : LocationService {
    override val mapTilerConfig: StateFlow<MapTilerConfig?> = flow {
        customMapTilerConfigProvider.get().getOrNull()?.let { emit(it) }
    }.stateIn(
        scope = sessionCoroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = buildTimeMapTilerConfig(),
    )
}
