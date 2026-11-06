/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.roomdetails.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import com.echo.features.call.test.FakeElementCallEntryPoint
import com.echo.features.knockrequests.test.FakeKnockRequestsListEntryPoint
import com.echo.features.messages.test.FakeMessagesEntryPoint
import com.echo.features.poll.test.history.FakePollHistoryEntryPoint
import com.echo.features.reportroom.test.FakeReportRoomEntryPoint
import com.echo.features.rolesandpermissions.test.FakeChangeRoomMemberRolesEntryPoint
import com.echo.features.rolesandpermissions.test.FakeRolesAndPermissionsEntryPoint
import com.echo.features.roomdetails.api.RoomDetailsEntryPoint
import com.echo.features.roomdetailsedit.test.FakeRoomDetailsEditEntryPoint
import com.echo.features.securityandprivacy.test.FakeSecurityAndPrivacyEntryPoint
import com.echo.features.verifysession.test.FakeOutgoingVerificationEntryPoint
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.permalink.PermalinkData
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.room.FakeJoinedRoom
import com.echo.libraries.mediaviewer.test.FakeMediaGalleryEntryPoint
import com.echo.libraries.mediaviewer.test.FakeMediaViewerEntryPoint
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultRoomDetailsEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultRoomDetailsEntryPoint()

        val parentNode = TestParentNode.create { buildContext, plugins ->
            RoomDetailsFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                pollHistoryEntryPoint = FakePollHistoryEntryPoint(),
                elementCallEntryPoint = FakeElementCallEntryPoint(),
                room = FakeJoinedRoom(),
                analyticsService = FakeAnalyticsService(),
                messagesEntryPoint = FakeMessagesEntryPoint(),
                knockRequestsListEntryPoint = FakeKnockRequestsListEntryPoint(),
                mediaViewerEntryPoint = FakeMediaViewerEntryPoint(),
                mediaGalleryEntryPoint = FakeMediaGalleryEntryPoint(),
                outgoingVerificationEntryPoint = FakeOutgoingVerificationEntryPoint(),
                reportRoomEntryPoint = FakeReportRoomEntryPoint(),
                changeRoomMemberRolesEntryPoint = FakeChangeRoomMemberRolesEntryPoint(),
                rolesAndPermissionsEntryPoint = FakeRolesAndPermissionsEntryPoint(),
                securityAndPrivacyEntryPoint = FakeSecurityAndPrivacyEntryPoint(),
                roomDetailsEditEntryPoint = FakeRoomDetailsEditEntryPoint(),
            )
        }
        val callback = object : RoomDetailsEntryPoint.Callback {
            override fun onDone() = lambdaError()
            override fun navigateToGlobalNotificationSettings() = lambdaError()
            override fun navigateToDeveloperSettings() = lambdaError()
            override fun navigateToRoom(roomId: RoomId, serverNames: List<String>, clearBackStack: Boolean) = lambdaError()
            override fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean) = lambdaError()
            override fun startForwardEventFlow(eventId: EventId) = lambdaError()
        }
        val params = RoomDetailsEntryPoint.Params(
            initialElement = RoomDetailsEntryPoint.InitialTarget.RoomDetails,
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(RoomDetailsFlowNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }

    @Test
    fun `test initial target to nav target mapping`() {
        assertThat(RoomDetailsEntryPoint.InitialTarget.RoomDetails.toNavTarget())
            .isEqualTo(RoomDetailsFlowNode.NavTarget.RoomDetails)
        assertThat(RoomDetailsEntryPoint.InitialTarget.RoomMemberDetails(A_USER_ID).toNavTarget())
            .isEqualTo(RoomDetailsFlowNode.NavTarget.RoomMemberDetails(A_USER_ID))
        assertThat(RoomDetailsEntryPoint.InitialTarget.RoomNotificationSettings.toNavTarget())
            .isEqualTo(RoomDetailsFlowNode.NavTarget.RoomNotificationSettings(showUserDefinedSettingStyle = true))
    }
}

