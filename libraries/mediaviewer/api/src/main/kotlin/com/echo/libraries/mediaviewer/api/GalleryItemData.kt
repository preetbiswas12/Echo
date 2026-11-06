/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.api

import android.os.Parcelable
import com.echo.libraries.matrix.api.media.MediaSource
import kotlinx.parcelize.Parcelize

@Parcelize
data class GalleryItemData(
    val filename: String,
    val mimeType: String,
    val mediaSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val type: Type,
    val blurHash: String?,
) : Parcelable {
    enum class Type {
        Image,
        Video,
        Audio,
        File,
    }
}

