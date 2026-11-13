/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.labs

import com.echo.features.preferences.impl.tasks.ClearCacheUseCase
import com.echo.features.preferences.impl.tasks.FakeClearCacheUseCase
import com.echo.libraries.featureflag.api.Feature
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.featureflag.test.FakeFeature
import com.echo.libraries.featureflag.test.FakeFeatureFlagService
import com.echo.services.toolbox.test.strings.FakeStringProvider
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import com.echo.tests.testutils.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LabsPresenterTest {
    @Test
    fun `present - ensures features are displayed in the correct order`() = runTest {
        val availableFeatures = listOf(
            FakeFeature(
                key = "feature_1",
                title = "Feature 1",
                isInLabs = true,
            ),
            FakeFeature(
                key = "feature_2",
                title = "Feature 3",
                isInLabs = true,
            )
        )
        val getAvailableFeaturesResult = lambdaRecorder<Boolean, Boolean, List<Feature>> { _, _ ->
            availableFeatures
        }
        createLabsPresenter(
            getAvailableFeaturesResult = getAvailableFeaturesResult,
        ).test {
            skipItems(1)
            val receivedFeatures = awaitItem().features
            assertThat(receivedFeatures).hasSize(2)
            assertThat(receivedFeatures[0].key).isEqualTo(availableFeatures[0].key)
            assertThat(receivedFeatures[1].key).isEqualTo(availableFeatures[1].key)
            getAvailableFeaturesResult.assertions().isCalledOnce()
                .with(value(false), value(true))
        }
    }

    @Test
    fun `present - ToggleFeature actually toggles the value`() = runTest {
        val availableFeatures = listOf(
            FakeFeature(
                key = "feature_1",
                title = "Feature 1",
                isInLabs = true,
            ),
        )
        createLabsPresenter(
            getAvailableFeaturesResult = { _, _ -> availableFeatures },
        ).test {
            skipItems(1)
            val initialItem = awaitItem()
            val feature = initialItem.features.first()
            assertThat(feature.isEnabled).isFalse()
            // Toggle the feature, should be true now
            initialItem.eventSink(LabsEvent.ToggleFeature(feature))
            assertThat(awaitItem().features.first().isEnabled).isTrue()
            // Toggle the feature, should be false now
            initialItem.eventSink(LabsEvent.ToggleFeature(feature))
            assertThat(awaitItem().features.first().isEnabled).isFalse()
        }
    }

    @Test
    fun `present - ToggleFeature with the 'Threads' feature resets the cache`() = runTest {
        val availableFeatures = listOf(
            FakeFeature(
                key = FeatureFlags.Threads.key,
                title = "Threads",
                isInLabs = true,
            ),
        )

        val clearCacheUseCase = FakeClearCacheUseCase()
        createLabsPresenter(
            getAvailableFeaturesResult = { _, _ -> availableFeatures },
            clearCacheUseCase = clearCacheUseCase,
        ).test {
            skipItems(1)
            val initialItem = awaitItem()
            val feature = initialItem.features.first()
            assertThat(feature.isEnabled).isFalse()
            assertThat(initialItem.isApplyingChanges).isFalse()
            // Toggle the feature
            initialItem.eventSink(LabsEvent.ToggleFeature(feature))
            assertThat(awaitItem().features.first().isEnabled).isTrue()
            // The clear cache use case should have been called
            assertThat(awaitItem().isApplyingChanges).isTrue()
            assertThat(clearCacheUseCase.executeHasBeenCalled).isTrue()
        }
    }

    private fun createLabsPresenter(
        getAvailableFeaturesResult: (Boolean, Boolean) -> List<Feature> = { _, _ -> emptyList() },
        clearCacheUseCase: ClearCacheUseCase = FakeClearCacheUseCase(),
    ): LabsPresenter {
        return LabsPresenter(
            stringProvider = FakeStringProvider(),
            featureFlagService = FakeFeatureFlagService(getAvailableFeaturesResult = getAvailableFeaturesResult),
            clearCacheUseCase = clearCacheUseCase,
        )
    }
}
