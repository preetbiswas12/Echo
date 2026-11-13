/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.location.test

import com.echo.features.enterprise.api.remoteconfig.MapTilerConfig
import com.echo.features.location.api.LocationService
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLocationService(
    mapTilerConfig: MapTilerConfig? = null,
) : LocationService {
    constructor(isServiceAvailable: Boolean) : this(
        MapTilerConfig(
            apiKey = "test_api_key",
            lightStyleId = null,
            darkStyleId = null,
            baseUrl = null,
        ).takeIf { isServiceAvailable }
    )

    override val mapTilerConfig = MutableStateFlow(mapTilerConfig)
}
