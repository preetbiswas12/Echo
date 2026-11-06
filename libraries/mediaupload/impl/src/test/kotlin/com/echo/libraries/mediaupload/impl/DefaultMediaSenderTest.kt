/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaupload.impl

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.echo.libraries.core.mimetype.MimeTypes
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.media.FileInfo
import com.echo.libraries.matrix.api.media.ImageInfo
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.test.A_THREAD_ID
import com.echo.libraries.matrix.test.media.FakeMediaUploadHandler
import com.echo.libraries.matrix.test.room.FakeJoinedRoom
import com.echo.libraries.matrix.test.timeline.FakeTimeline
import com.echo.libraries.mediaupload.api.MediaOptimizationConfig
import com.echo.libraries.mediaupload.api.MediaOptimizationConfigProvider
import com.echo.libraries.mediaupload.api.MediaPreProcessor
import com.echo.libraries.mediaupload.test.FakeMediaPreProcessor
import com.echo.libraries.preferences.api.store.VideoCompressionPreset
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class DefaultMediaSenderTest : RobolectricTest() {
    private val mediaOptimizationConfig = MediaOptimizationConfig(
        compressImages = true,
        videoCompressionPreset = VideoCompressionPreset.STANDARD,
    )

    @Test
    fun `given an attachment when sending it the preprocessor always runs`() = runTest {
        val preProcessor = FakeMediaPreProcessor()
        val sender = createDefaultMediaSender(
            preProcessor = preProcessor,
            room = FakeJoinedRoom(
                liveTimeline = FakeTimeline().apply {
                    sendFileLambda = lambdaRecorder<
                        File,
                        FileInfo,
                        String?,
                        String?,
                        EventId?,
                        Result<FakeMediaUploadHandler>,
                        > { _, _, _, _, _ ->
                        Result.success(FakeMediaUploadHandler())
                    }
                },
            )
        )

        val uri = Uri.parse("content://image.jpg")
        sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)

        assertThat(preProcessor.processCallCount).isEqualTo(1)
    }

    @Test
    fun `given an attachment when sending it the Room will call sendMedia`() = runTest {
        val sendImageResult =
            lambdaRecorder { _: File, _: File?, _: ImageInfo, _: String?, _: String?, _: EventId? ->
                Result.success(FakeMediaUploadHandler())
            }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendImageLambda = sendImageResult
            },
        )
        val sender = createDefaultMediaSender(room = room)

        val uri = Uri.parse("content://image.jpg")
        sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)
    }

    @Test
    fun `given a failure in the preprocessor when sending the whole process fails`() = runTest {
        val preProcessor = FakeMediaPreProcessor().apply {
            givenResult(Result.failure(Exception()))
        }
        val sender = createDefaultMediaSender(preProcessor)

        val uri = Uri.parse("content://image.jpg")
        val result = sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)

        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @Test
    fun `given a failure in the media upload when sending the whole process fails`() = runTest {
        val preProcessor = FakeMediaPreProcessor().apply {
            givenImageResult()
        }
        val sendImageResult =
            lambdaRecorder { _: File, _: File?, _: ImageInfo, _: String?, _: String?, _: EventId? ->
                Result.failure<FakeMediaUploadHandler>(Exception())
            }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendImageLambda = sendImageResult
            },
        )
        val sender = createDefaultMediaSender(
            preProcessor = preProcessor,
            room = room,
        )

        val uri = Uri.parse("content://image.jpg")
        val result = sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)

        assertThat(result.exceptionOrNull()).isNotNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given a cancellation in the media upload when sending the job is cancelled`() = runTest(StandardTestDispatcher()) {
        val sendFileResult =
            lambdaRecorder<File, FileInfo, String?, String?, EventId?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
                Result.success(FakeMediaUploadHandler())
            }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendFileLambda = sendFileResult
            },
        )
        val sender = createDefaultMediaSender(room = room)
        val sendJob = launch {
            val uri = Uri.parse("content://image.jpg")
            sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)
        }
        // Wait until several internal tasks run and the file is being uploaded
        advanceTimeBy(3L)

        // Assert the file is being uploaded
        assertThat(sender.hasOngoingMediaUploads).isTrue()

        // Cancel the coroutine
        sendJob.cancel()

        // Wait for the coroutine cleanup to happen
        advanceTimeBy(1L)

        // Assert the file is not being uploaded anymore
        assertThat(sender.hasOngoingMediaUploads).isFalse()
        sendFileResult.assertions().isCalledOnce()
    }

    @Test
    fun `given a thread, the created timeline is closed once the media is sent`() = runTest {
        val threadedTimeline = FakeTimeline(mode = Timeline.Mode.Thread(A_THREAD_ID)).apply {
            sendFileLambda = { _, _, _, _, _ -> Result.success(FakeMediaUploadHandler()) }
        }
        val liveTimeline = FakeTimeline()
        val room = FakeJoinedRoom(
            liveTimeline = liveTimeline,
            createTimelineResult = { Result.success(threadedTimeline) },
        )
        val sender = createDefaultMediaSender(
            room = room,
            timelineMode = Timeline.Mode.Thread(A_THREAD_ID),
        )

        val uri = Uri.parse("content://image.jpg")
        val result = sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)

        assertThat(result.isSuccess).isTrue()
        assertThat(threadedTimeline.closeCounter).isEqualTo(1)
        assertThat(liveTimeline.closeCounter).isEqualTo(0)
    }

    @Test
    fun `given a live timeline, the timeline is not closed once the media is sent`() = runTest {
        val liveTimeline = FakeTimeline().apply {
            sendFileLambda = { _, _, _, _, _ -> Result.success(FakeMediaUploadHandler()) }
        }
        val sender = createDefaultMediaSender(room = FakeJoinedRoom(liveTimeline = liveTimeline))

        val uri = Uri.parse("content://image.jpg")
        val result = sender.sendMedia(uri = uri, mimeType = MimeTypes.Jpeg, mediaOptimizationConfig = mediaOptimizationConfig)

        assertThat(result.isSuccess).isTrue()
        assertThat(liveTimeline.closeCounter).isEqualTo(0)
    }

    private fun createDefaultMediaSender(
        preProcessor: MediaPreProcessor = FakeMediaPreProcessor(),
        room: JoinedRoom = FakeJoinedRoom(),
        timelineMode: Timeline.Mode = Timeline.Mode.Live,
        mediaOptimizationConfigProvider: MediaOptimizationConfigProvider = MediaOptimizationConfigProvider { mediaOptimizationConfig },
    ) = DefaultMediaSender(
        preProcessor = preProcessor,
        room = room,
        timelineMode = timelineMode,
        mediaOptimizationConfigProvider = mediaOptimizationConfigProvider,
    )
}

