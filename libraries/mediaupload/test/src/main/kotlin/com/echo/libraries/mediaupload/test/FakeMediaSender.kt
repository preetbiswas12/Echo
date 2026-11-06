/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaupload.test

import android.net.Uri
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.mediaupload.api.MediaOptimizationConfig
import com.echo.libraries.mediaupload.api.MediaSender
import com.echo.libraries.mediaupload.api.MediaUploadInfo
import com.echo.tests.testutils.lambda.lambdaError

class FakeMediaSender(
    private val preProcessMediaResult: () -> Result<MediaUploadInfo> = { lambdaError() },
    private val sendPreProcessedMediaResult: () -> Result<Unit> = { lambdaError() },
    private val sendMediaResult: () -> Result<Unit> = { lambdaError() },
    private val sendVoiceMessageResult: () -> Result<Unit> = { lambdaError() },
    private val sendGalleryResult: () -> Result<Unit> = { lambdaError() },
    private val cleanUpResult: () -> Unit = { lambdaError() },
) : MediaSender {
    override suspend fun preProcessMedia(
        uri: Uri,
        mimeType: String,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<MediaUploadInfo> {
        return preProcessMediaResult()
    }

    override suspend fun sendPreProcessedMedia(
        mediaUploadInfo: MediaUploadInfo,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<Unit> {
        return sendPreProcessedMediaResult()
    }

    override suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<Unit> {
        return sendMediaResult()
    }

    override suspend fun sendVoiceMessage(
        uri: Uri,
        mimeType: String,
        waveForm: List<Float>,
        inReplyToEventId: EventId?,
    ): Result<Unit> {
        return sendVoiceMessageResult()
    }

    override suspend fun sendGallery(
        mediaUploadInfos: List<MediaUploadInfo>,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<Unit> {
        return sendGalleryResult()
    }

    override fun cleanUp() {
        cleanUpResult()
    }
}

