/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaupload.impl

import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.mediaupload.api.MaxUploadSizeProvider
import dev.zacsweers.metro.ContributesBinding

/**
 * Provides the maximum upload size allowed by the Matrix server.
 */
@ContributesBinding(SessionScope::class)
class DefaultMaxUploadSizeProvider(
    private val matrixClient: MatrixClient,
) : MaxUploadSizeProvider {
    override suspend fun getMaxUploadSize(): Result<Long> {
        return matrixClient.getMaxFileUploadSize()
    }
}
