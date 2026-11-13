/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.location.impl.show

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.echo.features.location.api.Location
import com.echo.features.location.api.ShowLocationEntryPoint
import com.echo.features.location.api.ShowLocationMode
import com.echo.features.location.impl.common.FakeUserLocationStateFactory
import com.echo.features.location.impl.common.actions.FakeLocationActions
import com.echo.features.location.impl.common.permissions.FakePermissionsPresenter
import com.echo.features.location.test.FakeActiveLiveLocationShareManager
import com.echo.features.location.test.FakeLocationService
import com.echo.libraries.dateformatter.test.FakeDateFormatter
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.test.core.aBuildMeta
import com.echo.libraries.matrix.test.room.FakeJoinedRoom
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.services.toolbox.test.strings.FakeStringProvider
import com.echo.tests.testutils.node.TestParentNode
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class DefaultShowLocationEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultShowLocationEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            val joinedRoom = FakeJoinedRoom()
            ShowLocationNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { mode ->
                    ShowLocationPresenter(
                        mode = mode,
                        permissionsPresenterFactory = { FakePermissionsPresenter() },
                        locationActions = FakeLocationActions(),
                        buildMeta = aBuildMeta(),
                        dateFormatter = FakeDateFormatter(),
                        stringProvider = FakeStringProvider(),
                        joinedRoom = joinedRoom,
                        liveLocationShareManager = FakeActiveLiveLocationShareManager(),
                        userLocationStateFactory = FakeUserLocationStateFactory(),
                        locationService = FakeLocationService(),
                    )
                },
                analyticsService = FakeAnalyticsService(),
            )
        }
        val inputs = ShowLocationEntryPoint.Inputs(
            mode = ShowLocationMode.Static(
                location = Location(37.4219983, -122.084, 10f),
                senderName = "Alice",
                senderId = UserId("@alice:matrix.org"),
                senderAvatarUrl = null,
                timestamp = System.currentTimeMillis(),
                assetType = null,
            ),
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = inputs,
        )
        assertThat(result).isInstanceOf(ShowLocationNode::class.java)
        assertThat(result.plugins).contains(inputs)
    }
}
