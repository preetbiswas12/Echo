/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl

import com.bumble.appyx.core.modality.BuildContext
import com.echo.features.home.api.HomeEntryPoint
import com.echo.features.invite.test.declineandblock.FakeDeclineInviteAndBlockEntryPoint
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.room.JoinedRoom
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import com.echo.tests.testutils.robolectric.RobolectricTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultHomeEntryPointTest : RobolectricTest() {
    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultHomeEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            HomeFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                matrixClient = FakeMatrixClient(),
                presenter = createHomePresenter(),
                inviteFriendsUseCase = { lambdaError() },
                analyticsService = FakeAnalyticsService(),
                acceptDeclineInviteView = { _, _, _, _ -> lambdaError() },
                reportRoomEntryPoint = { _, _, _ -> lambdaError() },
                declineInviteAndBlockUserEntryPoint = FakeDeclineInviteAndBlockEntryPoint(),
                changeRoomMemberRolesEntryPoint = { _, _, _, _ -> lambdaError() },
                leaveRoomRenderer = { _, _, _ -> lambdaError() },
                sessionCoroutineScope = backgroundScope,
            )
        }
        val callback = object : HomeEntryPoint.Callback {
            override fun navigateToRoom(roomId: RoomId, eventId: EventId?, joinedRoom: JoinedRoom?) = lambdaError()
            override fun navigateToCreateRoom() = lambdaError()
            override fun navigateToCreateSpace() = lambdaError()
            override fun navigateToSettings() = lambdaError()
            override fun navigateToSetUpRecovery() = lambdaError()
            override fun navigateToEnterRecoveryKey() = lambdaError()
            override fun navigateToRoomSettings(roomId: RoomId) = lambdaError()
            override fun navigateToBugReport() = lambdaError()
            override fun navigateToAddAccount() = lambdaError()
            override fun navigateToSession(sessionId: SessionId) = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            callback = callback,
        )
        assertThat(result).isInstanceOf(HomeFlowNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}
