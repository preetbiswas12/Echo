/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.local

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.echo.features.viewfolder.api.TextFileViewer
import com.echo.libraries.audio.api.AudioFocus
import com.echo.libraries.core.mimetype.MimeTypes
import com.echo.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import com.echo.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import com.echo.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import com.echo.libraries.mediaviewer.api.MediaInfo
import com.echo.libraries.mediaviewer.api.local.LocalMedia
import com.echo.libraries.mediaviewer.impl.local.audio.MediaAudioView
import com.echo.libraries.mediaviewer.impl.local.file.MediaFileView
import com.echo.libraries.mediaviewer.impl.local.image.MediaImageView
import com.echo.libraries.mediaviewer.impl.local.pdf.MediaPdfView
import com.echo.libraries.mediaviewer.impl.local.txt.TextFileView
import com.echo.libraries.mediaviewer.impl.local.video.MediaVideoView

@Composable
fun LocalMediaView(
    localMedia: LocalMedia?,
    bottomPaddingInPixels: Int,
    audioFocus: AudioFocus?,
    onClick: () -> Unit,
    onOpenWith: (() -> Unit)?,
    textFileViewer: TextFileViewer,
    forPreview: Boolean,
    modifier: Modifier = Modifier,
    isDisplayed: Boolean = true,
    isUserSelected: Boolean = false,
    localMediaViewState: LocalMediaViewState = rememberLocalMediaViewState(),
    mediaInfo: MediaInfo? = localMedia?.info,
) {
    val mimeType = mediaInfo?.mimeType
    when {
        mimeType.isMimeTypeImage() -> MediaImageView(
            localMediaViewState = localMediaViewState,
            localMedia = localMedia,
            modifier = modifier,
            forPreview = forPreview,
            onClick = onClick,
        )
        mimeType.isMimeTypeVideo() -> MediaVideoView(
            isDisplayed = isDisplayed,
            localMediaViewState = localMediaViewState,
            bottomPaddingInPixels = bottomPaddingInPixels,
            localMedia = localMedia,
            autoplay = isUserSelected,
            audioFocus = audioFocus,
            forPreview = forPreview,
            modifier = modifier,
        )
        mimeType == MimeTypes.PlainText || mimeType == MimeTypes.Json -> TextFileView(
            localMedia = localMedia,
            textFileViewer = textFileViewer,
            modifier = modifier,
        )
        mimeType == MimeTypes.Pdf -> MediaPdfView(
            localMediaViewState = localMediaViewState,
            localMedia = localMedia,
            modifier = modifier,
            onClick = onClick,
        )
        mimeType.isMimeTypeAudio() -> MediaAudioView(
            isDisplayed = isDisplayed,
            localMediaViewState = localMediaViewState,
            bottomPaddingInPixels = bottomPaddingInPixels,
            localMedia = localMedia,
            info = mediaInfo,
            audioFocus = audioFocus,
            modifier = modifier,
        )
        else -> MediaFileView(
            localMediaViewState = localMediaViewState,
            uri = localMedia?.uri,
            info = mediaInfo,
            modifier = modifier,
            onOpenWith = onOpenWith,
        )
    }
}

