/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.troubleshoot

import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.A_USER_ID_2
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import com.echo.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import com.echo.libraries.troubleshoot.test.runAndTestState
import com.echo.services.toolbox.test.strings.FakeStringProvider
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class IgnoredUsersTestTest {
    @Test
    fun `test IgnoredUsersTest order`() = runTest {
        val sut = IgnoredUsersTest(
            matrixClient = FakeMatrixClient(),
            stringProvider = FakeStringProvider(),
        )
        assertThat(sut.order).isEqualTo(80)
    }

    @Test
    fun `test IgnoredUsersTest quick fix`() = runTest {
        val sut = IgnoredUsersTest(
            matrixClient = FakeMatrixClient(),
            stringProvider = FakeStringProvider(),
        )
        val openIgnoredUsersResult = lambdaRecorder<Unit> {}
        val navigator = object : NotificationTroubleshootNavigator {
            override fun navigateToBlockedUsers() = openIgnoredUsersResult()
        }
        sut.quickFix(
            coroutineScope = backgroundScope,
            navigator = navigator,
        )
        openIgnoredUsersResult.assertions().isCalledOnce()
    }

    @Test
    fun `test IgnoredUsersTest with no blocked users`() = runTest {
        val sut = IgnoredUsersTest(
            matrixClient = FakeMatrixClient(
                ignoredUsersFlow = MutableStateFlow(persistentListOf())
            ),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test IgnoredUsersTest with blocked users`() = runTest {
        val sut = IgnoredUsersTest(
            matrixClient = FakeMatrixClient(
                ignoredUsersFlow = MutableStateFlow(persistentListOf(A_USER_ID, A_USER_ID_2))
            ),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            val lastStatus = lastItem.status as NotificationTroubleshootTestState.Status.Failure
            assertThat(lastStatus.hasQuickFix).isTrue()
            assertThat(lastStatus.isCritical).isFalse()
            assertThat(lastStatus.quickFixButtonString).isNotNull()
            assertThat(lastItem.description).contains("2")
        }
    }
}
