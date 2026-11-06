/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.messages.impl.R
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemVideoContentPreviewParam
import com.echo.features.messages.impl.timeline.model.event.aTimelineItemVideoContent
import com.echo.features.messages.impl.timeline.protection.ProtectedView
import com.echo.features.messages.impl.timeline.protection.coerceRatioWhenHidingContent
import com.echo.features.messages.impl.timeline.util.handleAsyncImageStateChange
import com.echo.libraries.designsystem.components.blurhash.blurHashBackground
import com.echo.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import com.echo.libraries.designsystem.modifiers.roundedBackground
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import com.echo.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import com.echo.libraries.matrix.ui.media.contentvalidation.collectOverallState
import com.echo.libraries.matrixmedia.api.MAX_THUMBNAIL_HEIGHT
import com.echo.libraries.matrixmedia.api.MAX_THUMBNAIL_WIDTH
import com.echo.libraries.matrixmedia.api.MediaRequestData
import com.echo.libraries.ui.strings.CommonStrings
import com.echo.libraries.ui.utils.a11y.isTalkbackActive

@Composable
fun TimelineItemVideoView(
    content: TimelineItemVideoContent,
    hideMediaContent: Boolean,
    onContentClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onShowContentClick: () -> Unit,
    contentValidationState: ContentValidationState,
    modifier: Modifier = Modifier,
) {
    val isTalkbackActive = isTalkbackActive()
    val a11yLabel = stringResource(CommonStrings.common_video)
    val description = content.caption?.let { "$a11yLabel: $it" } ?: a11yLabel
    Column(modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally)) {
        val containerModifier = if (content.showCaption) {
            Modifier
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(6.dp))
        } else {
            Modifier
        }

        val eventContentValidation by contentValidationState.collectOverallState()
        val isContentBeingValidated = !eventContentValidation.isValidated()
        TimelineItemAspectRatioBox(
            modifier = containerModifier.blurHashBackground(content.blurHash, alpha = 0.9f),
            aspectRatio = coerceRatioWhenHidingContent(content.aspectRatio, hideMediaContent),
            contentAlignment = Alignment.Center,
        ) {
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
                                if (!isTalkbackActive && onContentClick != null && !hasLoadError) {
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
                        model = MediaRequestData(
                            source = content.thumbnailSource,
                            kind = MediaRequestData.Kind.Thumbnail(
                                width = content.thumbnailWidth?.toLong() ?: MAX_THUMBNAIL_WIDTH,
                                height = content.thumbnailHeight?.toLong() ?: MAX_THUMBNAIL_HEIGHT,
                            )
                        ),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        contentDescription = if (hasLoadError) stringResource(R.string.screen_room_timeline_media_expired) else description,
                        onState = { state ->
                            hasLoadError = state is AsyncImagePainter.State.Error
                            val url = content.thumbnailSource?.safeUrl
                            if (url != null) {
                                handleAsyncImageStateChange(
                                    state = state,
                                    onLoaded = { isLoaded = true },
                                    updateContentValidationState = { contentValidationState.update(url, it) },
                                )
                            }
                        },
                    )

                    when {
                        hasLoadError -> {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.screen_room_timeline_media_expired),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                        !isContentBeingValidated -> {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .roundedBackground(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    imageVector = CompoundIcons.PlaySolid(),
                                    contentDescription = stringResource(id = CommonStrings.a11y_play),
                                    colorFilter = ColorFilter.tint(Color.White),
                                    modifier = Modifier.semantics { hideFromAccessibility() }
                                )
                            }
                        }
                        else -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVideoViewPreview(@PreviewParameter(TimelineItemVideoContentPreviewParam::class) content: TimelineItemVideoContent) = ElementPreview {
    TimelineItemVideoView(
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
internal fun TimelineItemVideoViewHideMediaContentPreview() = ElementPreview {
    TimelineItemVideoView(
        content = aTimelineItemVideoContent(),
        hideMediaContent = true,
        onShowContentClick = {},
        onContentClick = {},
        onLongClick = {},
        contentValidationState = NoopContentValidationState(),
    )
}

