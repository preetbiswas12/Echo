/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.x.di

import com.echo.libraries.architecture.NodeFactoriesBindings
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.MatrixClient
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(SessionScope::class)
interface SessionGraph : NodeFactoriesBindings {
    val roomGraphFactory: RoomGraph.Factory

    @GraphExtension.Factory
    interface Factory {
        fun create(@Provides matrixClient: MatrixClient): SessionGraph
    }
}
