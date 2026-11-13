/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.local.file

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.compound.theme.ElementTheme
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.libraries.core.bool.orFalse
import com.echo.libraries.core.mimetype.MimeTypes
import com.echo.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import com.echo.libraries.designsystem.components.BigIcon
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.ButtonSize
import com.echo.libraries.designsystem.theme.components.IconSource
import com.echo.libraries.designsystem.theme.components.OutlinedButton
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.mediaviewer.api.MediaInfo
import com.echo.libraries.mediaviewer.api.helper.formatFileExtensionAndSize
import com.echo.libraries.mediaviewer.impl.R
import com.echo.libraries.mediaviewer.impl.local.LocalMediaViewState
import com.echo.libraries.mediaviewer.impl.local.rememberLocalMediaViewState
import com.echo.libraries.ui.strings.CommonStrings

/**
 * Ref: https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=3361-16623
 */
@Composable
fun MediaFileView(
    localMediaViewState: LocalMediaViewState,
    uri: Uri?,
    info: MediaInfo?,
    onOpenWith: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isAudio = info?.mimeType.isMimeTypeAudio().orFalse()
    localMediaViewState.isReady = uri != null
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val isApk = info?.mimeType == MimeTypes.Apk
            val icon = when {
                isAudio -> CompoundIcons.Audio()
                isApk -> CompoundIcons.Bug()
                else -> CompoundIcons.Files()
            }
            BigIcon(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = BigIcon.Style.Default(
                    vectorIcon = icon,
                ),
            )
            if (info != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = info.filename,
                    maxLines = 2,
                    style = ElementTheme.typography.fontBodyLgRegular,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = ElementTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatFileExtensionAndSize(info.fileExtension, info.formattedFileSize),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ElementTheme.colors.textPrimary
                )
                if (onOpenWith != null) {
                    val (icon, textResId) = if (isApk) {
                        IconSource.Resource(R.drawable.ic_apk_install) to CommonStrings.common_install_apk_android
                    } else {
                        IconSource.Vector(CompoundIcons.PopOut()) to CommonStrings.action_open_with
                    }
                    OutlinedButton(
                        modifier = Modifier.padding(top = 24.dp),
                        size = ButtonSize.Small,
                        leadingIcon = icon,
                        onClick = onOpenWith,
                        text = stringResource(id = textResId),
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun MediaFileViewPreview(
    @PreviewParameter(MediaInfoFilePreviewParam::class) info: MediaInfo
) = ElementPreview {
    MediaFileView(
        modifier = Modifier.fillMaxSize(),
        localMediaViewState = rememberLocalMediaViewState(),
        uri = null,
        info = info,
        onOpenWith = {},
    )
}
