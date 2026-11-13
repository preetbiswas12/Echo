/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.attachments.preview.error

import com.echo.features.messages.impl.R
import com.echo.libraries.mediaupload.api.MediaPreProcessor

fun sendAttachmentError(
    throwable: Throwable
): Int {
    return if (throwable is MediaPreProcessor.Failure) {
        R.string.screen_media_upload_preview_error_failed_processing
    } else {
        R.string.screen_media_upload_preview_error_failed_sending
    }
}
