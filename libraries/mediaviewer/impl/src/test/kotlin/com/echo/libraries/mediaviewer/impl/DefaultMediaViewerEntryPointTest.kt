/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import com.echo.features.contentscanner.api.ContentScannerService
import com.echo.features.enterprise.test.FakeEnterpriseService
import com.echo.libraries.audio.test.FakeAudioFocus
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.media.MediaSource
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.matrix.test.media.FakeMatrixMediaLoader
import com.echo.libraries.matrix.ui.media.contentvalidation.NoopEventContentValidationCache
import com.echo.libraries.mediaviewer.api.AvatarInfo
import com.echo.libraries.mediaviewer.api.MediaViewerEntryPoint
import com.echo.libraries.mediaviewer.impl.datasource.createTimelineMediaGalleryDataSource
import com.echo.libraries.mediaviewer.impl.viewer.MediaViewerNode
import com.echo.libraries.mediaviewer.impl.viewer.PagerKeysHandler
import com.echo.libraries.mediaviewer.impl.viewer.createMediaViewerEntryPointParams
import com.echo.libraries.mediaviewer.impl.viewer.createMediaViewerPresenter
import com.echo.libraries.mediaviewer.test.FakeLocalMediaFactory
import com.echo.services.toolbox.test.systemclock.FakeSystemClock
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import com.echo.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMediaViewerEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() = runTest {
        Dispatchers.setMain(testCoroutineDispatchers().main)
        val entryPoint = DefaultMediaViewerEntryPoint()
        val mockMediaUri: Uri = mockk("localMediaUri")
        val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)
        val parentNode = TestParentNode.create { buildContext, plugins ->
            MediaViewerNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { _, _, _ ->
                    createMediaViewerPresenter(
                        localMediaFactory = localMediaFactory,
                    )
                },
                timelineMediaGalleryDataSource = createTimelineMediaGalleryDataSource(),
                focusedTimelineMediaGalleryDataSourceFactory = { _, _, _ ->
                    lambdaError()
                },
                mediaLoader = FakeMatrixMediaLoader(),
                localMediaFactory = FakeLocalMediaFactory(mockMediaUri),
                coroutineDispatchers = testCoroutineDispatchers(),
                systemClock = FakeSystemClock(),
                pagerKeysHandler = PagerKeysHandler(),
                textFileViewer = { _, _ -> lambdaError() },
                audioFocus = FakeAudioFocus(),
                sessionId = A_SESSION_ID,
                enterpriseService = FakeEnterpriseService(),
                contentValidationCache = NoopEventContentValidationCache(),
                contentScannerService = ContentScannerService { _, _ -> },
            )
        }
        val callback = object : MediaViewerEntryPoint.Callback {
            override fun onDone() = lambdaError()
            override fun viewInTimeline(eventId: EventId) = lambdaError()
            override fun forwardEvent(eventId: EventId) = lambdaError()
        }
        val params = createMediaViewerEntryPointParams()
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(MediaViewerNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }

    @Test
    fun `test node builder avatar`() = runTest {
        Dispatchers.setMain(testCoroutineDispatchers().main)
        val entryPoint = DefaultMediaViewerEntryPoint()
        val mockMediaUri: Uri = mockk("localMediaUri")
        val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)
        val parentNode = TestParentNode.create { buildContext, plugins ->
            MediaViewerNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { _, _, _ ->
                    createMediaViewerPresenter(
                        localMediaFactory = localMediaFactory,
                    )
                },
                timelineMediaGalleryDataSource = createTimelineMediaGalleryDataSource(),
                focusedTimelineMediaGalleryDataSourceFactory = { _, _, _ ->
                    lambdaError()
                },
                mediaLoader = FakeMatrixMediaLoader(),
                localMediaFactory = FakeLocalMediaFactory(mockMediaUri),
                coroutineDispatchers = testCoroutineDispatchers(),
                systemClock = FakeSystemClock(),
                pagerKeysHandler = PagerKeysHandler(),
                textFileViewer = { _, _ -> lambdaError() },
                audioFocus = FakeAudioFocus(),
                sessionId = A_SESSION_ID,
                enterpriseService = FakeEnterpriseService(),
                contentValidationCache = NoopEventContentValidationCache(),
                contentScannerService = ContentScannerService { _, _ -> },
            )
        }
        val callback = object : MediaViewerEntryPoint.Callback {
            override fun onDone() = lambdaError()
            override fun viewInTimeline(eventId: EventId) = lambdaError()
            override fun forwardEvent(eventId: EventId) = lambdaError()
        }
        val params = entryPoint.createParamsForAvatar(
            filename = "avatar.png",
            avatarUrl = "avatarUrl",
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(MediaViewerNode::class.java)
        assertThat(result.plugins).contains(
            MediaViewerEntryPoint.Params.Avatar(
                avatarInfo = AvatarInfo(filename = "avatar.png"),
                mediaSource = MediaSource(url = "avatarUrl"),
                thumbnailSource = null,
                blurHash = null,
            )
        )
        assertThat(result.plugins).contains(callback)
    }
}

