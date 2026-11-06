/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.compound.theme.ElementTheme
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.libraries.designsystem.atomic.atoms.PlaybackSpeedButton
import com.echo.libraries.designsystem.components.media.WaveformPlaybackView
import com.echo.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.CircularProgressIndicator
import com.echo.libraries.designsystem.theme.components.Icon
import com.echo.libraries.designsystem.theme.components.IconButton
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import com.echo.libraries.mediaviewer.impl.model.MediaItem
import com.echo.libraries.mediaviewer.impl.model.aMediaItemVoice
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.libraries.voiceplayer.api.VoiceMessageEvent
import com.echo.libraries.voiceplayer.api.VoiceMessageState
import com.echo.libraries.voiceplayer.api.VoiceMessageStatePreviewParam
import com.echo.libraries.voiceplayer.api.aVoiceMessageState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

@Composable
fun VoiceItemView(
    state: VoiceMessageState,
    voice: MediaItem.Voice,
    onLongClick: () -> Unit,
    contentValidationValue: ContentValidationValue,
    modifier: Modifier = Modifier,
) {
    GalleryFileListItem(
        modifier = modifier,
        contentValidationValue = contentValidationValue,
        caption = voice.mediaInfo.caption,
    ) {
        VoiceInfoRow(
            state = state,
            voice = voice,
            isValidating = contentValidationValue.isLoading(),
            onLongClick = onLongClick,
        )
    }
}

@Composable
private fun VoiceInfoRow(
    state: VoiceMessageState,
    voice: MediaItem.Voice,
    isValidating: Boolean,
    onLongClick: () -> Unit,
) {
    fun playPause() {
        state.eventSink(VoiceMessageEvent.PlayPause)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = ElementTheme.colors.bgSubtleSecondary,
                shape = RoundedCornerShape(12.dp),
            )
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick,
                onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
            )
            .onKeyboardContextMenuAction(onLongClick)
            .fillMaxWidth()
            .padding(start = 12.dp, end = 36.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isValidating) {
            CustomIconButton(onClick = {}, enabled = false) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            when (state.buttonType) {
                VoiceMessageState.ButtonType.Play -> PlayButton(onClick = ::playPause)
                VoiceMessageState.ButtonType.Pause -> PauseButton(onClick = ::playPause)
                VoiceMessageState.ButtonType.Downloading -> ProgressButton()
                VoiceMessageState.ButtonType.Retry -> RetryButton(onClick = ::playPause)
                VoiceMessageState.ButtonType.Disabled -> PlayButton(onClick = {}, enabled = false)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            PlaybackSpeedButton(
                speed = state.playbackSpeed,
                onClick = { state.eventSink(VoiceMessageEvent.ChangePlaybackSpeed) }.takeIf { !isValidating } ?: {},
            )
            Text(
                text = if (state.progress > 0f) state.time else voice.mediaInfo.duration ?: state.time,
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodyMdMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        WaveformPlaybackView(
            modifier = Modifier
                .weight(1f)
                .height(34.dp),
            showCursor = state.showCursor,
            playbackProgress = state.progress,
            waveform = voice.mediaInfo.waveform.orEmpty().toImmutableList(),
            onSeek = {
                state.eventSink(VoiceMessageEvent.Seek(it))
            },
            isPlaying = state.isPlaying,
            durationMs = state.durationMs,
            playbackSpeed = state.playbackSpeed,
            seekEnabled = !isValidating,
        )
    }
}

/**
 * Progress button is shown when the voice message is being downloaded.
 *
 * The progress indicator is optimistic and displays a pause button (which
 * indicates the audio is playing) for 2 seconds before revealing the
 * actual progress indicator.
 */
@Composable
private fun ProgressButton(
    displayImmediately: Boolean = false,
) {
    var canDisplay by remember { mutableStateOf(displayImmediately) }
    LaunchedEffect(Unit) {
        delay(2000L)
        canDisplay = true
    }
    CustomIconButton(
        onClick = {},
        enabled = false,
    ) {
        if (canDisplay) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(2.dp)
                    .size(16.dp),
                color = ElementTheme.colors.iconSecondary,
                strokeWidth = 2.dp,
            )
        } else {
            ControlIcon(
                imageVector = CompoundIcons.PauseSolid(),
                contentDescription = stringResource(id = CommonStrings.a11y_pause),
            )
        }
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    CustomIconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        ControlIcon(
            imageVector = CompoundIcons.PlaySolid(),
            contentDescription = stringResource(id = CommonStrings.a11y_play),
        )
    }
}

@Composable
private fun PauseButton(
    onClick: () -> Unit,
) {
    CustomIconButton(
        onClick = onClick,
    ) {
        ControlIcon(
            imageVector = CompoundIcons.PauseSolid(),
            contentDescription = stringResource(id = CommonStrings.a11y_pause),
        )
    }
}

@Composable
private fun RetryButton(
    onClick: () -> Unit,
) {
    CustomIconButton(
        onClick = onClick,
    ) {
        ControlIcon(
            imageVector = CompoundIcons.Restart(),
            contentDescription = stringResource(id = CommonStrings.action_retry),
        )
    }
}

@Composable
private fun ControlIcon(
    imageVector: ImageVector,
    contentDescription: String?,
) {
    Icon(
        modifier = Modifier.padding(vertical = 10.dp),
        imageVector = imageVector,
        contentDescription = contentDescription,
    )
}

@Composable
private fun CustomIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(color = ElementTheme.colors.bgCanvasDefault, shape = CircleShape)
            .border(
                width = 1.dp,
                color = ElementTheme.colors.borderInteractiveSecondary,
                shape = CircleShape,
            )
            .size(36.dp),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = ElementTheme.colors.iconSecondary,
            disabledContentColor = ElementTheme.colors.iconDisabled,
        ),
        content = content,
    )
}

@PreviewsDayNight
@Composable
internal fun VoiceItemViewPreview(
    @PreviewParameter(MediaItemVoicePreviewParam::class) voice: MediaItem.Voice,
) = ElementPreview {
    val states = remember {
        listOf(
            ContentValidationValue.Valid,
            ContentValidationValue.Loading,
            ContentValidationValue.Invalid,
            ContentValidationValue.UnrecoverableError(Throwable("Unrecoverable error")),
        )
    }
    Column {
        for (state in states) {
            VoiceItemView(
                state = aVoiceMessageState(),
                voice = voice,
                onLongClick = {},
                contentValidationValue = state,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun VoiceItemViewPlayPreview(
    @PreviewParameter(VoiceMessageStatePreviewParam::class) state: VoiceMessageState,
) = ElementPreview {
    val validationState = remember {
        listOf(
            ContentValidationValue.Valid,
            ContentValidationValue.Loading,
            ContentValidationValue.Invalid,
            ContentValidationValue.UnrecoverableError(Throwable("Unrecoverable error")),
        )
    }
    Column {
        for (validationState in validationState) {
            VoiceItemView(
                state = state,
                voice = aMediaItemVoice(),
                onLongClick = {},
                contentValidationValue = validationState,
            )
        }
    }
}

