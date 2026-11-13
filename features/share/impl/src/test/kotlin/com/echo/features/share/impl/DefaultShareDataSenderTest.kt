/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.share.impl

import android.net.Uri
import com.echo.features.share.api.OnSharedData
import com.echo.features.share.api.ShareIntentData
import com.echo.features.share.api.UriToShare
import com.echo.libraries.core.mimetype.MimeTypes
import com.echo.libraries.matrix.test.A_MESSAGE
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.room.FakeJoinedRoom
import com.echo.libraries.matrix.test.timeline.FakeTimeline
import com.echo.libraries.mediaupload.api.MediaOptimizationConfigProviderFactory
import com.echo.libraries.mediaupload.api.MediaSenderRoomFactory
import com.echo.libraries.mediaupload.test.FakeMediaOptimizationConfigProvider
import com.echo.libraries.mediaupload.test.FakeMediaSender
import com.echo.services.appnavstate.api.ActiveRoomsHolder
import com.echo.services.appnavstate.impl.DefaultActiveRoomsHolder
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.robolectric.RobolectricTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultShareDataSenderTest : RobolectricTest() {
    @Test
    fun `send text ok`() = runTest {
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendMessageLambda = { _, _, _, _, _ -> Result.success(Unit) }
            },
        )
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, joinedRoom)
        }
        val onSharedData = lambdaRecorder<ShareIntentData, Unit> { }
        val sender = createDefaultShareDataSender(
            onSharedData = { onSharedData(it) },
        )
        val result = sender.send(matrixClient, ShareIntentData.PlainText(A_MESSAGE), listOf(A_ROOM_ID))
        assertThat(result.isSuccess).isTrue()
        onSharedData.assertions().isCalledOnce()
    }

    @Test
    fun `send text, room not found`() = runTest {
        val onSharedData = lambdaRecorder<ShareIntentData, Unit> { }
        val sender = createDefaultShareDataSender(
            onSharedData = { onSharedData(it) },
        )
        val result = sender.send(FakeMatrixClient(), ShareIntentData.PlainText(A_MESSAGE), listOf(A_ROOM_ID))
        assertThat(result.isFailure).isTrue()
        onSharedData.assertions().isCalledOnce()
    }

    @Test
    fun `send media ok`() = runTest {
        val sendMediaResult = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = FakeTimeline(),
        )
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, joinedRoom)
        }
        val mediaSender = FakeMediaSender(
            sendMediaResult = sendMediaResult,
        )
        val sender = createDefaultShareDataSender(
            mediaSenderRoomFactory = { _, _ -> mediaSender },
        )
        val result = sender.send(
            client = matrixClient,
            shareIntentData = ShareIntentData.Uris(
                text = A_MESSAGE,
                listOf(
                    UriToShare(
                        uri = Uri.parse("content://image.jpg"),
                        mimeType = MimeTypes.Jpeg,
                    )
                )
            ),
            roomIds = listOf(A_ROOM_ID),
        )
        assertThat(result.isSuccess).isTrue()
        sendMediaResult.assertions().isCalledOnce()
    }

    @Test
    fun `send media with no uris is a failure`() = runTest {
        val sender = createDefaultShareDataSender()
        val result = sender.send(
            client = FakeMatrixClient(),
            shareIntentData = ShareIntentData.Uris(text = A_MESSAGE, emptyList()),
            roomIds = listOf(A_ROOM_ID),
        )
        assertThat(result.isFailure).isTrue()
    }
}

private fun createDefaultShareDataSender(
    mediaSenderRoomFactory: MediaSenderRoomFactory = MediaSenderRoomFactory { _, _ -> FakeMediaSender() },
    mediaOptimizationConfigProviderFactory: MediaOptimizationConfigProviderFactory = MediaOptimizationConfigProviderFactory {
        FakeMediaOptimizationConfigProvider()
    },
    activeRoomsHolder: ActiveRoomsHolder = DefaultActiveRoomsHolder(),
    onSharedData: OnSharedData = OnSharedData {},
) = DefaultShareDataSender(
    mediaSenderRoomFactory = mediaSenderRoomFactory,
    mediaOptimizationConfigProviderFactory = mediaOptimizationConfigProviderFactory,
    activeRoomsHolder = activeRoomsHolder,
    onSharedData = onSharedData,
)
