/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl

import com.echo.libraries.core.extensions.mapFailure
import com.echo.libraries.core.extensions.runCatchingExceptions
import com.echo.libraries.matrix.api.TemporaryMatrixClient
import com.echo.libraries.matrix.api.paths.SessionPaths
import com.echo.libraries.matrix.impl.exception.mapClientException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client

class RustTemporaryMatrixClient(
    private val client: Client,
    private val paths: SessionPaths?,
    private val dispatcher: CoroutineDispatcher,
) : TemporaryMatrixClient {
    override suspend fun getUrl(url: String): Result<ByteArray> = withContext(dispatcher) {
        runCatchingExceptions {
            client.getUrl(url)
        }.mapFailure { it.mapClientException() }
    }

    override val server: String? = client.server()
    override val homeserverUrl: String = client.homeserver()

    override fun close() {
        client.close()
        paths?.let {
            if (it.fileDirectory.exists()) {
                it.fileDirectory.deleteRecursively()
            }
            if (it.cacheDirectory.exists()) {
                it.cacheDirectory.deleteRecursively()
            }
        }
    }
}

