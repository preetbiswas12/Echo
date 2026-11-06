/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.server

import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.server.UserServerResolver

@ContributesBinding(SessionScope::class)
class DefaultUserServerResolver(
    private val matrixClient: MatrixClient,
) : UserServerResolver {
    override fun resolve(): String {
        return matrixClient.userIdServerName()
    }
}

