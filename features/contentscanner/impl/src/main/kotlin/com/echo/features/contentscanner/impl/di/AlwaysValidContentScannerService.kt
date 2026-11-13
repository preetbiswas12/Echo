/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.contentscanner.impl.di

import com.echo.features.contentscanner.api.ContentScannerService
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationValue

/**
 * Noop implementation of [ContentScannerService] that always returns success.
 *
 * This is used when the content scanner feature is not enabled or available.
 */
class AlwaysValidContentScannerService : ContentScannerService {
    override fun scan(mediaSources: List<MediaSource>, contentValidationState: ContentValidationState) {
        // Always return success for the noop implementation
        for (mediaSource in mediaSources) {
            val url = mediaSource.safeUrl
            contentValidationState.update(url, ContentValidationValue.Valid)
        }
    }
}
