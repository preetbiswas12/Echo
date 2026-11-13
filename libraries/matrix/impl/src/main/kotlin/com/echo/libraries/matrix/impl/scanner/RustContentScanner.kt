/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.scanner

import com.echo.libraries.core.extensions.mapFailure
import com.echo.libraries.core.extensions.runCatchingExceptions
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.libraries.matrix.api.scanner.ContentScanner
import com.echo.libraries.matrix.impl.exception.mapClientException
import com.echo.libraries.matrix.impl.media.toRustMediaSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ContentScanner as RustScanner

class RustContentScanner(
    private val client: Client,
    private val rustScanner: RustScanner,
    private val dispatcher: CoroutineDispatcher,
) : ContentScanner {
    override suspend fun scan(mediaSource: MediaSource): Result<Boolean> = withContext(dispatcher) {
        runCatchingExceptions {
            rustScanner.scan(client, mediaSource.toRustMediaSource()).clean
        }.mapFailure {
            it.mapClientException()
        }
    }
}
