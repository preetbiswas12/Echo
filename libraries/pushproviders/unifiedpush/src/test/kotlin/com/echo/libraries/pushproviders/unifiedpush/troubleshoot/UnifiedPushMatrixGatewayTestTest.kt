/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush.troubleshoot

import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.pushproviders.api.Config
import com.echo.libraries.pushproviders.test.aSessionPushConfig
import com.echo.libraries.pushproviders.unifiedpush.FakeUnifiedPushApiFactory
import com.echo.libraries.pushproviders.unifiedpush.UnifiedPushConfig
import com.echo.libraries.pushproviders.unifiedpush.invalidDiscoveryResponse
import com.echo.libraries.pushproviders.unifiedpush.matrixDiscoveryResponse
import com.echo.libraries.pushproviders.unifiedpush.network.DiscoveryResponse
import com.echo.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import com.echo.libraries.troubleshoot.api.test.TestFilterData
import com.echo.libraries.troubleshoot.test.runAndTestState
import com.echo.tests.testutils.testCoroutineDispatchers
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UnifiedPushMatrixGatewayTestTest {
    @Test
    fun `test UnifiedPushMatrixGatewayTest success`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest(
            config = aSessionPushConfig(),
            discoveryResponse = matrixDiscoveryResponse,
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test UnifiedPushMatrixGatewayTest no config found`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest(
            config = null,
            discoveryResponse = matrixDiscoveryResponse,
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure())
        }
    }

    @Test
    fun `test UnifiedPushMatrixGatewayTest not valid gateway`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest(
            config = aSessionPushConfig(),
            discoveryResponse = invalidDiscoveryResponse,
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure())
            // Reset the error
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
        }
    }

    @Test
    fun `test UnifiedPushMatrixGatewayTest network error`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest(
            config = aSessionPushConfig(),
            discoveryResponse = { error("Network error") },
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure())
        }
    }

    @Test
    fun `test isRelevant`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = UnifiedPushConfig.NAME))).isTrue()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = "other"))).isFalse()
    }

    private fun TestScope.createUnifiedPushMatrixGatewayTest(
        sessionId: SessionId = A_SESSION_ID,
        config: Config? = null,
        discoveryResponse: () -> DiscoveryResponse = matrixDiscoveryResponse,
    ): UnifiedPushMatrixGatewayTest {
        return UnifiedPushMatrixGatewayTest(
            sessionId = sessionId,
            unifiedPushApiFactory = FakeUnifiedPushApiFactory(discoveryResponse),
            coroutineDispatchers = testCoroutineDispatchers(),
            unifiedPushSessionPushConfigProvider = FakeUnifiedPushSessionPushConfigProvider(
                config = { config }
            ),
        )
    }
}
