/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.test

import android.net.Uri
import com.echo.libraries.core.mimetype.MimeTypes
import com.echo.libraries.matrix.api.media.MediaFile
import com.echo.libraries.mediaviewer.api.MediaInfo
import com.echo.libraries.mediaviewer.api.local.LocalMedia
import com.echo.libraries.mediaviewer.api.local.LocalMediaFactory
import com.echo.libraries.mediaviewer.api.util.FileExtensionExtractor
import com.echo.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import com.echo.libraries.mediaviewer.test.viewer.aLocalMedia

class FakeLocalMediaFactory(
    private val localMediaUri: Uri,
    private val fileExtensionExtractor: FileExtensionExtractor = FileExtensionExtractorWithoutValidation()
) : LocalMediaFactory {
    var fallbackMimeType: String = MimeTypes.OctetStream
    var fallbackName: String = "File name"
    var fallbackFileSize = "0B"

    override fun createFromMediaFile(mediaFile: MediaFile, mediaInfo: MediaInfo): LocalMedia {
        return aLocalMedia(uri = localMediaUri, mediaInfo = mediaInfo)
    }

    override fun createFromUri(
        uri: Uri,
        mimeType: String?,
        name: String?,
        formattedFileSize: String?
    ): LocalMedia {
        val safeName = name ?: fallbackName
        val mediaInfo = MediaInfo(
            filename = safeName,
            fileSize = null,
            caption = null,
            htmlCaption = null,
            mimeType = mimeType ?: fallbackMimeType,
            formattedFileSize = formattedFileSize ?: fallbackFileSize,
            fileExtension = fileExtensionExtractor.extractFromName(safeName),
            senderId = null,
            senderName = null,
            senderAvatar = null,
            dateSent = null,
            dateSentFull = null,
            waveform = null,
            duration = null,
        )
        return aLocalMedia(uri, mediaInfo)
    }
}

