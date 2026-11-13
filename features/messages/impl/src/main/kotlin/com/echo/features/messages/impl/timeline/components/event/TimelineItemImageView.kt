/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.echo.features.messages.impl.R
import com.echo.features.messages.impl.timeline.aTimelineItemEvent
import com.echo.features.messages.impl.timeline.components.ATimelineItemEventRow
import com.echo.features.messages.impl.timeline.model.TimelineItemGroupPosition
import com.echo.features.messages.impl.timeline.model.event.TimelineItemImageContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemImageContentPreviewParam
import com.echo.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import com.echo.features.messages.impl.timeline.protection.ProtectedView
import com.echo.features.messages.impl.timeline.protection.coerceRatioWhenHidingContent
import com.echo.features.messages.impl.timeline.util.handleAsyncImageStateChange
import com.echo.libraries.designsystem.components.blurhash.blurHashBackground
import com.echo.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.CircularProgressIndicator
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import com.echo.libraries.matrix.ui.media.contentvalidation.LocalEventContentValidationState
import com.echo.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import com.echo.libraries.matrix.ui.media.contentvalidation.NoopEventContentValidationCache
import com.echo.libraries.matrix.ui.media.contentvalidation.collectOverallState
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.libraries.ui.utils.a11y.isTalkbackActive

@Composable
fun TimelineItemImageView(
    content: TimelineItemImageContent,
    hideMediaContent: Boolean,
    onContentClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onShowContentClick: () -> Unit,
    contentValidationState: ContentValidationState,
    modifier: Modifier = Modifier,
) {
    val a11yLabel = stringResource(CommonStrings.common_image)
    val description = content.caption?.let { "$a11yLabel: $it" } ?: a11yLabel
    Column(modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally)) {
        val containerModifier = if (content.showCaption) {
            Modifier.clip(RoundedCornerShape(10.dp))
        } else {
            Modifier
        }

        val eventContentValidation by contentValidationState.collectOverallState()
        val isContentBeingValidated = !eventContentValidation.isValidated()
        TimelineItemAspectRatioBox(
            modifier = containerModifier
                .blurHashBackground(content.blurhash, alpha = 0.9f)
                .align(Alignment.CenterHorizontally),
            aspectRatio = coerceRatioWhenHidingContent(content.aspectRatio, hideMediaContent),
        ) {
            if (isContentBeingValidated) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                ProtectedView(
                    hideContent = hideMediaContent,
                    onShowClick = onShowContentClick,
                ) {
                    var isLoaded by remember { mutableStateOf(false) }
                    var hasLoadError by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isLoaded) Modifier.background(Color.White) else Modifier)
                                .then(
                                    if (!isTalkbackActive() && onContentClick != null && !hasLoadError) {
                                        Modifier
                                            .combinedClickable(
                                                onClick = onContentClick,
                                                onLongClick = onLongClick,
                                            )
                                            .onKeyboardContextMenuAction(onLongClick)
                                    } else {
                                        Modifier
                                    }
                                ),
                            model = content.thumbnailMediaRequestData,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            contentDescription = if (hasLoadError) stringResource(R.string.screen_room_timeline_media_expired) else description,
                            onState = { state ->
                                hasLoadError = state is AsyncImagePainter.State.Error
                                val url = content.thumbnailMediaRequestData.source?.safeUrl
                                if (url != null) {
                                    handleAsyncImageStateChange(
                                        state = state,
                                        onLoaded = { isLoaded = true },
                                        updateContentValidationState = { contentValidationState.update(url, it) },
                                    )
                                }
                            },
                        )
                        if (hasLoadError) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                androidx.compose.material3.Text(
                                    text = stringResource(R.string.screen_room_timeline_media_expired),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemImageViewPreview(@PreviewParameter(TimelineItemImageContentPreviewParam::class) content: TimelineItemImageContent) = ElementPreview {
    TimelineItemImageView(
        content = content,
        hideMediaContent = false,
        onShowContentClick = {},
        onContentClick = {},
        onLongClick = {},
        contentValidationState = NoopContentValidationState(),
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemImageViewHideMediaContentPreview() = ElementPreview {
        TimelineItemImageView(
            content = aTimelineItemImageContent(),
            hideMediaContent = true,
            onShowContentClick = {},
            onContentClick = {},
            onLongClick = {},
            contentValidationState = NoopContentValidationState(),
        )
}

@PreviewsDayNight
@Composable
internal fun ATimelineItemEventRowPreview() = ElementPreview {
    CompositionLocalProvider(LocalEventContentValidationState provides NoopEventContentValidationCache()) {
        Column {
            sequenceOf(false, true).forEach { isMine ->
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        isMine = isMine,
                        content = aTimelineItemImageContent(
                            filename = "image.jpg",
                            caption = "A long caption that may wrap into several lines",
                            width = 40,
                            height = 20,
                            aspectRatio = 40f / 20f,
                        ),
                        groupPosition = TimelineItemGroupPosition.Last,
                    ),
                )
            }
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = false,
                    content = aTimelineItemImageContent(
                        filename = "image.jpg",
                        caption = "Narrow image with null aspectRatio",
                        width = 80,
                        height = 150,
                        aspectRatio = null,
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}
