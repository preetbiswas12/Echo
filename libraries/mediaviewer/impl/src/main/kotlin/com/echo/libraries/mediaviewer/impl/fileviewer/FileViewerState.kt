/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.fileviewer

import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.designsystem.utils.snackbar.SnackbarMessage
import com.echo.libraries.mediaviewer.api.local.LocalMedia

data class FileViewerState(
    val filename: String,
    val localMedia: AsyncData<LocalMedia>,
    val snackbarMessage: SnackbarMessage?,
    val eventSink: (FileViewerEvent) -> Unit,
)

