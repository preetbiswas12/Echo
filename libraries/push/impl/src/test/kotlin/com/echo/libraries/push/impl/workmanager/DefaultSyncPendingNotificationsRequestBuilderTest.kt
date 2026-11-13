/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.workmanager

import android.net.NetworkCapabilities
import androidx.work.OneTimeWorkRequest
import androidx.work.hasKeyWithValueOfType
import com.echo.features.networkmonitor.test.FakeNetworkMonitor
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.featureflag.test.FakeFeatureFlagService
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.workmanager.api.WorkManagerRequestType
import com.echo.libraries.workmanager.api.WorkManagerWorkerType
import com.echo.libraries.workmanager.api.workManagerTag
import com.echo.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import com.echo.tests.testutils.robolectric.RobolectricTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [33])
class DefaultSyncPendingNotificationsRequestBuilderTest : RobolectricTest() {
    @Test
    fun `build - success API 31`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 31,
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()
        results.getOrNull()!!.first().let { result ->
            assertThat(result.type).isInstanceOf(WorkManagerWorkerType.Unique::class.java)
            result.request.run {
                assertThat(this).isInstanceOf(OneTimeWorkRequest::class.java)
                assertThat(workSpec.input.hasKeyWithValueOfType<String>(SyncPendingNotificationsRequestBuilder.SESSION_ID)).isTrue()
                assertThat(workSpec.hasConstraints()).isTrue()
                // True in API 31+
                assertThat(workSpec.expedited).isTrue()
                assertThat(workSpec.traceTag).isEqualTo(workManagerTag(A_SESSION_ID, WorkManagerRequestType.NOTIFICATION_SYNC))
            }
        }
    }

    @Test
    fun `build - success API 30 and lower`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 30,
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()

        results.getOrNull()!!.first().let { result ->
            assertThat(result.type).isInstanceOf(WorkManagerWorkerType.Unique::class.java)
            result.request.run {
                assertThat(this).isInstanceOf(OneTimeWorkRequest::class.java)
                assertThat(workSpec.input.hasKeyWithValueOfType<String>(SyncPendingNotificationsRequestBuilder.SESSION_ID)).isTrue()
                assertThat(workSpec.hasConstraints()).isTrue()
                // False before API 31
                assertThat(workSpec.expedited).isFalse()
                assertThat(workSpec.traceTag).isEqualTo(workManagerTag(A_SESSION_ID, WorkManagerRequestType.NOTIFICATION_SYNC))
            }
        }
    }

    @Test
    fun `build - has NET_CAPABILITY_VALIDATED constraint if not in air-gapped env`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 33,
            isInAirGapEnvironment = false,
            featureFlagService = FakeFeatureFlagService(initialState = mapOf(
                FeatureFlags.ValidateNetworkWhenSchedulingNotificationFetching.key to true
            )),
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()
        results.getOrNull()!!.first().let { result ->
            result.request.run {
                assertThat(workSpec.hasConstraints()).isTrue()
                val networkRequest = workSpec.constraints.requiredNetworkRequest
                assertThat(networkRequest).isNotNull()
                assertThat(networkRequest!!.capabilities.contains(NetworkCapabilities.NET_CAPABILITY_VALIDATED)).isTrue()
            }
        }
    }

    @Test
    fun `build - does not have NET_CAPABILITY_VALIDATED constraint if in air-gapped env`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 33,
            isInAirGapEnvironment = true,
            featureFlagService = FakeFeatureFlagService(initialState = mapOf(
                FeatureFlags.ValidateNetworkWhenSchedulingNotificationFetching.key to true
            )),
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()
        results.getOrNull()!!.first().let { result ->
            result.request.run {
                assertThat(workSpec.hasConstraints()).isTrue()
                val networkRequest = workSpec.constraints.requiredNetworkRequest
                assertThat(networkRequest).isNotNull()
                assertThat(networkRequest!!.capabilities.contains(NetworkCapabilities.NET_CAPABILITY_VALIDATED)).isFalse()
            }
        }
    }

    @Test
    fun `build - does not have NET_CAPABILITY_VALIDATED constraint if feature flag is disabled`() = runTest {
        val request = createSyncPendingNotificationsRequestBuilder(
            sessionId = A_SESSION_ID,
            sdkVersion = 33,
            isInAirGapEnvironment = false,
            featureFlagService = FakeFeatureFlagService(initialState = mapOf(
                FeatureFlags.ValidateNetworkWhenSchedulingNotificationFetching.key to false
            )),
        )

        val results = request.build()
        assertThat(results.isSuccess).isTrue()
        results.getOrNull()!!.first().let { result ->
            result.request.run {
                assertThat(workSpec.hasConstraints()).isTrue()
                val networkRequest = workSpec.constraints.requiredNetworkRequest
                assertThat(networkRequest).isNotNull()
                assertThat(networkRequest!!.capabilities.contains(NetworkCapabilities.NET_CAPABILITY_VALIDATED)).isFalse()
            }
        }
    }
}

private fun createSyncPendingNotificationsRequestBuilder(
    sessionId: SessionId,
    sdkVersion: Int = 33,
    isInAirGapEnvironment: Boolean = false,
    featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
) = DefaultSyncPendingNotificationsRequestBuilder(
    sessionId = sessionId,
    buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(sdkVersion),
    networkMonitor = FakeNetworkMonitor().apply { givenIsInAirGappedEnvironment(isInAirGapEnvironment) },
    featureFlagService = featureFlagService,
)
