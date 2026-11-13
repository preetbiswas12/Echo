/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.location.impl

import com.echo.features.enterprise.api.remoteconfig.CustomMapTilerConfigProvider
import com.echo.features.enterprise.test.remoteconfig.aMapTilerConfig
import com.echo.features.location.api.buildTimeMapTilerConfig
import com.echo.libraries.matrix.test.AN_EXCEPTION
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultLocationServiceTest {
    @Test
    fun `when the homeserver does not provide a config, the build time config is used`() = runTest {
        val locationService = createDefaultLocationService(
            customMapTilerConfigProvider = { Result.success(null) },
        )
        runCurrent()
        assertThat(locationService.mapTilerConfig.value).isEqualTo(buildTimeMapTilerConfig())
        assertThat(locationService.isServiceAvailable().value).isEqualTo(buildTimeMapTilerConfig() != null)
    }

    @Test
    fun `when the homeserver config cannot be retrieved, the build time config is used`() = runTest {
        val locationService = createDefaultLocationService(
            customMapTilerConfigProvider = { Result.failure(AN_EXCEPTION) },
        )
        runCurrent()
        assertThat(locationService.mapTilerConfig.value).isEqualTo(buildTimeMapTilerConfig())
        assertThat(locationService.isServiceAvailable().value).isEqualTo(buildTimeMapTilerConfig() != null)
    }

    @Test
    fun `when the homeserver provides a config, it is used and the service is available`() = runTest {
        val config = aMapTilerConfig(apiKey = "homeserver_api_key")
        val locationService = createDefaultLocationService(
            customMapTilerConfigProvider = { Result.success(config) },
        )
        runCurrent()
        assertThat(locationService.mapTilerConfig.value).isEqualTo(config)
        assertThat(locationService.isServiceAvailable().value).isTrue()
    }

    private fun TestScope.createDefaultLocationService(
        customMapTilerConfigProvider: CustomMapTilerConfigProvider,
        sessionCoroutineScope: CoroutineScope = backgroundScope,
    ) = DefaultLocationService(
        customMapTilerConfigProvider = customMapTilerConfigProvider,
        sessionCoroutineScope = sessionCoroutineScope,
    )
}
