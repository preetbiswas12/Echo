/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrixmedia.impl

import com.echo.libraries.designsystem.components.avatar.AvatarData
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.libraries.matrixmedia.api.AVATAR_THUMBNAIL_SIZE_IN_PIXEL
import com.echo.libraries.matrixmedia.api.MediaRequestData

internal fun AvatarData.toMediaRequestData(): MediaRequestData {
    return MediaRequestData(
        source = url?.let { MediaSource(it) },
        kind = MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)
    )
}
