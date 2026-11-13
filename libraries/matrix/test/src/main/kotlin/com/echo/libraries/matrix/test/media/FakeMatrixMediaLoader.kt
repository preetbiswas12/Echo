/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test.media

import com.echo.libraries.matrix.api.media.MatrixMediaLoader
import com.echo.libraries.matrix.api.media.MediaFile
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.tests.testutils.simulateLongTask

class FakeMatrixMediaLoader : MatrixMediaLoader {
    var shouldFail = false
    var path: String = ""

    override suspend fun loadMediaContent(source: MediaSource): Result<ByteArray> = simulateLongTask {
        if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            Result.success(ByteArray(0))
        }
    }

    override suspend fun loadMediaThumbnail(source: MediaSource, width: Long, height: Long): Result<ByteArray> = simulateLongTask {
        if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            Result.success(ByteArray(0))
        }
    }

    override suspend fun downloadMediaFile(
        source: MediaSource,
        mimeType: String?,
        filename: String?,
        useCache: Boolean,
    ): Result<MediaFile> = simulateLongTask {
        if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            Result.success(FakeMediaFile(path))
        }
    }
}
