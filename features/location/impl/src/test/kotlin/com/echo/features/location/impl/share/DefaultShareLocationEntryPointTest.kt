/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.location.impl.share

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import com.echo.features.location.impl.common.FakeUserLocationStateFactory
import com.echo.features.location.impl.common.actions.FakeLocationActions
import com.echo.features.location.impl.common.permissions.FakePermissionsPresenter
import com.echo.features.location.impl.live.LiveLocationStore
import com.echo.features.location.test.FakeActiveLiveLocationShareManager
import com.echo.features.location.test.FakeLocationService
import com.echo.features.messages.test.FakeMessageComposerContext
import com.echo.libraries.dateformatter.test.FakeDurationFormatter
import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.core.aBuildMeta
import com.echo.libraries.matrix.test.room.FakeJoinedRoom
import com.echo.libraries.preferences.test.FakePreferenceDataStoreFactory
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultShareLocationEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultShareLocationEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            val room = FakeJoinedRoom()
            ShareLocationNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { timelineMode: Timeline.Mode ->
                    ShareLocationPresenter(
                        permissionsPresenterFactory = { FakePermissionsPresenter() },
                        room = room,
                        timelineMode = timelineMode,
                        analyticsService = FakeAnalyticsService(),
                        messageComposerContext = FakeMessageComposerContext(),
                        locationActions = FakeLocationActions(),
                        buildMeta = aBuildMeta(),
                        client = FakeMatrixClient(),
                        durationFormatter = FakeDurationFormatter(),
                        liveLocationShareManager = FakeActiveLiveLocationShareManager(),
                        liveLocationStore = LiveLocationStore(
                            preferenceDataStoreFactory = FakePreferenceDataStoreFactory(),
                            sessionId = room.sessionId,
                        ),
                        userLocationStateFactory = FakeUserLocationStateFactory(),
                        locationService = FakeLocationService(),
                    )
                },
                analyticsService = FakeAnalyticsService(),
            )
        }
        val timelineMode = Timeline.Mode.Live
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            timelineMode = timelineMode,
        )
        assertThat(result).isInstanceOf(ShareLocationNode::class.java)
        assertThat(result.plugins).contains(ShareLocationNode.Inputs(timelineMode))
    }
}

