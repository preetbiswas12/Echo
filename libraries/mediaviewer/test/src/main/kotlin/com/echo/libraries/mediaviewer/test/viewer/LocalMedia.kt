/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.test.viewer

import android.net.Uri
import com.echo.libraries.mediaviewer.api.MediaInfo
import com.echo.libraries.mediaviewer.api.anImageMediaInfo
import com.echo.libraries.mediaviewer.api.local.LocalMedia

fun aLocalMedia(
    uri: Uri,
    mediaInfo: MediaInfo = anImageMediaInfo(),
) = LocalMedia(
    uri = uri,
    info = mediaInfo
)
