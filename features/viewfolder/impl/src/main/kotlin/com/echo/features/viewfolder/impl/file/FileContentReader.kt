/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.viewfolder.impl.file

import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.core.extensions.runCatchingExceptions
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext
import java.io.File

interface FileContentReader {
    suspend fun getLines(path: String): Result<List<String>>
}

@ContributesBinding(AppScope::class)
class DefaultFileContentReader(
    private val dispatchers: CoroutineDispatchers,
) : FileContentReader {
    override suspend fun getLines(path: String): Result<List<String>> = withContext(dispatchers.io) {
        runCatchingExceptions {
            File(path).readLines()
        }
    }
}
