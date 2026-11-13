/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.media

import com.echo.libraries.androidutils.file.safeDelete
import com.echo.libraries.core.extensions.runCatchingExceptions
import com.echo.libraries.matrix.api.media.MediaUploadHandler
import org.matrix.rustcomponents.sdk.SendGalleryJoinHandle
import java.io.File

class GalleryMediaUploadHandlerImpl(
    private val filesToUpload: List<File>,
    private val sendGalleryJoinHandle: SendGalleryJoinHandle,
) : MediaUploadHandler {
    override suspend fun await(): Result<Unit> =
        runCatchingExceptions {
            sendGalleryJoinHandle.join()
        }
            .also { cleanUpFiles() }

    override fun cancel() {
        sendGalleryJoinHandle.cancel()
        cleanUpFiles()
    }

    private fun cleanUpFiles() {
        filesToUpload.forEach { file -> file.safeDelete() }
    }
}
