/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.platform

import com.echo.libraries.core.meta.BuildMeta
import com.echo.libraries.matrix.api.platform.InitPlatformService
import com.echo.libraries.matrix.api.tracing.TracingConfiguration
import com.echo.libraries.matrix.impl.tracing.map
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import org.matrix.rustcomponents.sdk.initPlatform

@ContributesBinding(AppScope::class)
class RustInitPlatformService(
    private val buildMeta: BuildMeta,
) : InitPlatformService {
    override fun init(tracingConfiguration: TracingConfiguration) {
        initPlatform(
            config = tracingConfiguration.map(buildMeta),
            useLightweightTokioRuntime = false
        )
    }
}
