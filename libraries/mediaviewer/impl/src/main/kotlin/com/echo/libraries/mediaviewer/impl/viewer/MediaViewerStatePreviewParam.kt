/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.viewer

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.designsystem.components.media.WaveFormSamples
import com.echo.libraries.designsystem.preview.USER_NAME_ALICE
import com.echo.libraries.designsystem.text.toAnnotatedString
import com.echo.libraries.htmlrenderer.api.DocumentNode
import com.echo.libraries.htmlrenderer.api.ParagraphNode
import com.echo.libraries.htmlrenderer.api.QuoteNode
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import com.echo.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import com.echo.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import com.echo.libraries.mediaviewer.api.MediaInfo
import com.echo.libraries.mediaviewer.api.aPdfMediaInfo
import com.echo.libraries.mediaviewer.api.aTxtMediaInfo
import com.echo.libraries.mediaviewer.api.aVideoMediaInfo
import com.echo.libraries.mediaviewer.api.anApkMediaInfo
import com.echo.libraries.mediaviewer.api.anAudioMediaInfo
import com.echo.libraries.mediaviewer.api.anImageMediaInfo
import com.echo.libraries.mediaviewer.api.local.LocalMedia
import com.echo.libraries.mediaviewer.impl.details.MediaBottomSheetState
import com.echo.libraries.mediaviewer.impl.details.aMediaBottomSheetStateDeleteConfirmation
import com.echo.libraries.mediaviewer.impl.details.aMediaBottomSheetStateDetails
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private const val LONG_CAPTION = "This is a very long caption that should be scrollable in the media viewer. " +
    "It contains multiple lines of text to demonstrate the scrolling behavior. " +
    "Line 1: Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
    "Line 2: Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
    "Line 3: Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris. " +
    "Line 4: Duis aute irure dolor in reprehenderit in voluptate velit esse cillum. " +
    "Line 5: Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia."

open class MediaViewerStatePreviewParam : PreviewParameterProvider<MediaViewerState> {
    override val values: Sequence<MediaViewerState>
        get() = sequenceOf(
            aMediaViewerState(),
            aMediaViewerState(listOf(aMediaViewerPageData(AsyncData.Loading()))),
            aMediaViewerState(listOf(aMediaViewerPageData(AsyncData.Failure(IllegalStateException("error"))))),
            anImageMediaInfo(
                senderName = "Sally Sanderson",
                dateSent = "21 NOV, 2024",
                caption = "A caption",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aVideoMediaInfo(
                senderName = "A very long name so that it will be truncated and will not be displayed on multiple lines",
                dateSent = "A very very long date that will be truncated and will not be displayed on multiple lines",
                caption = "A caption",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aPdfMediaInfo().let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aMediaViewerState(
                listOf(
                    aMediaViewerPageData(
                        downloadedMedia = AsyncData.Loading(),
                        mediaInfo = anApkMediaInfo(),
                    )
                )
            ),
            anApkMediaInfo().let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aMediaViewerState(
                listOf(
                    aMediaViewerPageData(
                        downloadedMedia = AsyncData.Loading(),
                        mediaInfo = anAudioMediaInfo(),
                    )
                )
            ),
            anAudioMediaInfo().let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo().let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    ),
                    canShowInfo = false,
                )
            },
            aMediaViewerState(
                mediaBottomSheetState = aMediaBottomSheetStateDetails(),
            ),
            aMediaViewerState(
                mediaBottomSheetState = aMediaBottomSheetStateDeleteConfirmation(),
            ),
            anAudioMediaInfo(
                waveForm = WaveFormSamples.realisticWaveForm,
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aMediaViewerState(
                listOf(
                    aMediaViewerPageDataLoading()
                ),
            ),
            aMediaViewerState(
                listOf(
                    MediaViewerPageData.Failure(Exception("error"))
                ),
            ),
            aMediaViewerState(
                listOf(
                    aMediaViewerPageData(
                        downloadedMedia = AsyncData.Loading(),
                        mediaInfo = aTxtMediaInfo(),
                    )
                )
            ),
            anImageMediaInfo(
                senderName = USER_NAME_ALICE,
                dateSent = "21 NOV, 2024",
                caption = LONG_CAPTION,
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo(
                senderName = "Bob",
                dateSent = "22 NOV, 2024",
                htmlCaption = "This is a <strong>bold</strong> caption",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo(
                senderName = "Charlie",
                dateSent = "23 NOV, 2024",
                htmlCaption = "This is an <em>italic</em> caption",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo(
                senderName = "Diana",
                dateSent = "24 NOV, 2024",
                htmlCaption = "This is a <code>code</code> caption",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo(
                senderName = "Eve",
                dateSent = "25 NOV, 2024",
                htmlCaption = "<blockquote>This is a quote caption</blockquote>",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo(
                senderName = "Frank",
                dateSent = "26 NOV, 2024",
                htmlCaption = "This caption has <strong>bold</strong>, <em>italic</em>, and <code>code</code> formatting.",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo(
                senderName = "Frank",
                dateSent = "26 NOV, 2024",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Uninitialized,
                            validationState = NoopContentValidationState(ContentValidationValue.Loading),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo(
                senderName = "Frank",
                dateSent = "26 NOV, 2024",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Uninitialized,
                            validationState = NoopContentValidationState(ContentValidationValue.Invalid),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo(
                senderName = "Sally Sanderson",
                dateSent = "21 NOV, 2024",
                caption = "A caption",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                            captionTree = DocumentNode(
                                children = persistentListOf(
                                    QuoteNode(
                                        persistentListOf(
                                            ParagraphNode(
                                                text = "This is a formatted caption".toAnnotatedString()
                                            )
                                        )
                                    )
                                )
                            ),
                        )
                    )
                )
            }
        )
}

fun aMediaViewerPageDataLoading(
    direction: Timeline.PaginationDirection = Timeline.PaginationDirection.BACKWARDS,
    timestamp: Long = 0L,
): MediaViewerPageData {
    return MediaViewerPageData.Loading(
        direction = direction,
        timestamp = timestamp,
        pagerKey = 0L,
    )
}

fun aMediaViewerPageData(
    downloadedMedia: AsyncData<LocalMedia> = AsyncData.Uninitialized,
    mediaInfo: MediaInfo = anImageMediaInfo(),
    mediaSource: MediaSource = MediaSource(""),
    validationState: ContentValidationState = NoopContentValidationState(),
    formattedCaption: CharSequence? = mediaInfo.htmlCaption,
    captionTree: DocumentNode? = null,
): MediaViewerPageData.MediaViewerData = MediaViewerPageData.MediaViewerData(
    eventId = null,
    mediaInfo = mediaInfo,
    formattedCaption = formattedCaption,
    captionTree = captionTree,
    mediaSource = mediaSource,
    thumbnailSource = null,
    downloadedMedia = mutableStateOf(downloadedMedia),
    pagerKey = 0L,
    validationState = validationState,
)

fun aMediaViewerState(
    listData: List<MediaViewerPageData> = listOf(aMediaViewerPageData()),
    currentIndex: Int = 0,
    canShowInfo: Boolean = true,
    mediaBottomSheetState: MediaBottomSheetState = MediaBottomSheetState.Hidden,
    currentUserId: UserId? = null,
    eventSink: (MediaViewerEvent) -> Unit = {},
) = MediaViewerState(
    initiallySelectedEventId = EventId("\$a:b"),
    listData = listData.toImmutableList(),
    currentIndex = currentIndex,
    snackbarMessage = null,
    canShowInfo = canShowInfo,
    mediaBottomSheetState = mediaBottomSheetState,
    currentUserId = currentUserId,
    eventSink = eventSink,
)

