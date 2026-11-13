/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.attachments.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.net.toUri
import com.echo.features.messages.impl.attachments.Attachment
import com.echo.features.messages.impl.attachments.preview.imageeditor.AttachmentImageEditorState
import com.echo.features.messages.impl.attachments.preview.imageeditor.anAttachmentImageEditorState
import com.echo.features.messages.impl.attachments.video.MediaOptimizationSelectorState
import com.echo.features.messages.impl.attachments.video.VideoUploadEstimation
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.core.mimetype.MimeTypes
import com.echo.libraries.matrix.api.media.ImageInfo
import com.echo.libraries.mediaupload.api.MediaUploadInfo
import com.echo.libraries.mediaviewer.api.MediaInfo
import com.echo.libraries.mediaviewer.api.aVideoMediaInfo
import com.echo.libraries.mediaviewer.api.anImageMediaInfo
import com.echo.libraries.mediaviewer.api.local.LocalMedia
import com.echo.libraries.preferences.api.store.VideoCompressionPreset
import com.echo.libraries.textcomposer.model.TextEditorState
import com.echo.libraries.textcomposer.model.aTextEditorStateMarkdown
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.io.File

open class AttachmentsPreviewStatePreviewParam : PreviewParameterProvider<AttachmentsPreviewState> {
    override val values: Sequence<AttachmentsPreviewState>
        get() = sequenceOf(
            anAttachmentsPreviewState(),
            anAttachmentsPreviewState(
                sendActionState = SendActionState.Sending.Processing(displayProgress = false),
                textEditorState = aTextEditorStateMarkdown(
                    initialText = "This is a caption!"
                )
            ),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.Processing(displayProgress = true)),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.ReadyToUpload(listOf(aMediaUploadInfo()))),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.Uploading(listOf(aMediaUploadInfo()))),
            anAttachmentsPreviewState(sendActionState = SendActionState.Failure(RuntimeException("error"), listOf(aMediaUploadInfo()))),
            anAttachmentsPreviewState(
                imageEditorState = anAttachmentImageEditorState(),
            ),
            anAttachmentsPreviewState(displayFileTooLargeError = true),
            anAttachmentsPreviewState(
                mediaInfo = aVideoMediaInfo(),
                mediaOptimizationSelectorState = aMediaOptimisationSelectorState(
                    selectedVideoPreset = VideoCompressionPreset.STANDARD,
                    videoSizeEstimations = aVideoSizeEstimationList(),
                )
            ),
            anAttachmentsPreviewState(
                mediaInfo = aVideoMediaInfo(),
                mediaOptimizationSelectorState = aMediaOptimisationSelectorState(
                    videoSizeEstimations = aVideoSizeEstimationList(),
                    displayVideoPresetSelectorDialog = true,
                )
            ),
        )
}

fun anAttachmentsPreviewState(
    mediaInfo: MediaInfo = anImageMediaInfo(),
    textEditorState: TextEditorState = aTextEditorStateMarkdown(),
    sendActionState: SendActionState = SendActionState.Idle,
    imageEditorState: AttachmentImageEditorState? = null,
    mediaOptimizationSelectorState: MediaOptimizationSelectorState = aMediaOptimisationSelectorState(),
    displayFileTooLargeError: Boolean = false,
    currentIndex: Int = 0,
) = AttachmentsPreviewState(
    attachments = persistentListOf(
        Attachment.Media(
            localMedia = LocalMedia("file://path".toUri(), mediaInfo),
        ),
    ),
    imageEditorState = imageEditorState,
    canEditImage = true,
    isApplyingImageEdits = false,
    displayImageEditError = false,
    sendActionState = sendActionState,
    textEditorState = textEditorState,
    mediaOptimizationSelectorState = mediaOptimizationSelectorState,
    displayFileTooLargeError = displayFileTooLargeError,
    currentIndex = currentIndex,
    eventSink = {}
)

fun anAttachmentsPreviewGalleryState(
    mediaInfo: MediaInfo = anImageMediaInfo(),
    textEditorState: TextEditorState = aTextEditorStateMarkdown(),
    sendActionState: SendActionState = SendActionState.Idle,
    mediaOptimizationSelectorState: MediaOptimizationSelectorState = aMediaOptimisationSelectorState(),
    currentIndex: Int = 0,
) = AttachmentsPreviewState(
    attachments = persistentListOf(
        Attachment.Media(
            localMedia = LocalMedia("file://path1".toUri(), mediaInfo),
        ),
        Attachment.Media(
            localMedia = LocalMedia("file://path2".toUri(), mediaInfo),
        ),
        Attachment.Media(
            localMedia = LocalMedia("file://path3".toUri(), mediaInfo),
        ),
    ),
    imageEditorState = null,
    canEditImage = false,
    isApplyingImageEdits = false,
    displayImageEditError = false,
    sendActionState = sendActionState,
    textEditorState = textEditorState,
    mediaOptimizationSelectorState = mediaOptimizationSelectorState,
    displayFileTooLargeError = false,
    currentIndex = currentIndex,
    eventSink = {}
)

fun aMediaUploadInfo(
    filePath: String = "file://path",
    thumbnailFilePath: String? = null,
) = MediaUploadInfo.Image(
    file = File(filePath),
    imageInfo = ImageInfo(
        height = 100,
        width = 100,
        mimetype = MimeTypes.Jpeg,
        size = 1000,
        thumbnailInfo = null,
        thumbnailSource = null,
        blurhash = null,
    ),
    thumbnailFile = thumbnailFilePath?.let { File(it) },
)

fun aMediaOptimisationSelectorState(
    maxUploadSize: Long = 100 * 1024 * 1024,
    videoSizeEstimations: AsyncData<ImmutableList<VideoUploadEstimation>> = AsyncData.Success(persistentListOf()),
    isImageOptimizationEnabled: Boolean = true,
    selectedVideoPreset: VideoCompressionPreset = VideoCompressionPreset.STANDARD,
    displayMediaSelectorViews: Boolean = true,
    displayVideoPresetSelectorDialog: Boolean = false,
) = MediaOptimizationSelectorState(
    index = 0,
    maxUploadSize = AsyncData.Success(maxUploadSize),
    videoSizeEstimations = videoSizeEstimations,
    isImageOptimizationEnabled = isImageOptimizationEnabled,
    selectedVideoPreset = selectedVideoPreset,
    displayMediaSelectorViews = displayMediaSelectorViews,
    displayVideoPresetSelectorDialog = displayVideoPresetSelectorDialog,
    eventSink = {},
)

internal fun aVideoSizeEstimationList(): AsyncData<ImmutableList<VideoUploadEstimation>> = AsyncData.Success(
    persistentListOf(
        VideoUploadEstimation(
            preset = VideoCompressionPreset.HIGH,
            sizeInBytes = 8_200_000L,
            canUpload = false,
        ),
        VideoUploadEstimation(
            preset = VideoCompressionPreset.STANDARD,
            sizeInBytes = 4_200_000L,
            canUpload = true,
        ),
    )
)
