/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.x.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import com.echo.appnav.di.SessionGraphFactory
import com.echo.libraries.matrix.api.MatrixClient

@ContributesBinding(AppScope::class)
class DefaultSessionGraphFactory(
    private val appGraph: AppGraph,
) : SessionGraphFactory {
    override fun create(client: MatrixClient): Any {
        return appGraph.sessionGraphFactory.create(client)
    }
}

