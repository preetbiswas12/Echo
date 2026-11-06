/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.firebase.troubleshoot

import com.google.common.truth.Truth.assertThat
import com.echo.libraries.pushproviders.firebase.FakeFirebaseTroubleshooter
import com.echo.libraries.pushproviders.firebase.FirebaseConfig
import com.echo.libraries.pushproviders.firebase.InMemoryFirebaseStore
import com.echo.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import com.echo.libraries.troubleshoot.api.test.TestFilterData
import com.echo.libraries.troubleshoot.test.FakeNotificationTroubleshootNavigator
import com.echo.libraries.troubleshoot.test.runAndTestState
import com.echo.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FirebaseTokenTestTest {
    @Test
    fun `test FirebaseTokenTest success`() = runTest {
        val sut = FirebaseTokenTest(
            firebaseStore = InMemoryFirebaseStore(FAKE_TOKEN),
            firebaseTroubleshooter = FakeFirebaseTroubleshooter(),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
            assertThat(lastItem.description).contains(FAKE_TOKEN.takeLast(8))
            assertThat(lastItem.description).doesNotContain(FAKE_TOKEN)
        }
    }

    @Test
    fun `test FirebaseTokenTest error`() = runTest {
        val firebaseStore = InMemoryFirebaseStore(null)
        val sut = FirebaseTokenTest(
            firebaseStore = firebaseStore,
            firebaseTroubleshooter = FakeFirebaseTroubleshooter(
                troubleShootResult = {
                    firebaseStore.storeInstallationId(FAKE_TOKEN)
                    Result.success(Unit)
                }
            ),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true))
            // Quick fix
            sut.quickFix(this, FakeNotificationTroubleshootNavigator())
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test FirebaseTokenTest error and reset`() = runTest {
        val firebaseStore = InMemoryFirebaseStore(null)
        val sut = FirebaseTokenTest(
            firebaseStore = firebaseStore,
            firebaseTroubleshooter = FakeFirebaseTroubleshooter(
                troubleShootResult = {
                    firebaseStore.storeInstallationId(FAKE_TOKEN)
                    Result.success(Unit)
                }
            ),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true))
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
        }
    }

    @Test
    fun `test FirebaseTokenTest isRelevant`() {
        val sut = FirebaseTokenTest(
            firebaseStore = InMemoryFirebaseStore(null),
            firebaseTroubleshooter = FakeFirebaseTroubleshooter(),
            stringProvider = FakeStringProvider(),
        )
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = "unknown"))).isFalse()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = FirebaseConfig.NAME))).isTrue()
    }

    companion object {
        private const val FAKE_TOKEN = "abcdefghijk"
    }
}

