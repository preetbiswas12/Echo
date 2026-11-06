/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.battery

import androidx.lifecycle.Lifecycle
import com.google.common.truth.Truth.assertThat
import com.echo.libraries.push.api.battery.BatteryOptimizationEvent
import com.echo.libraries.push.impl.push.FakeMutableBatteryOptimizationStore
import com.echo.libraries.push.impl.push.MutableBatteryOptimizationStore
import com.echo.libraries.push.impl.store.InMemoryPushDataStore
import com.echo.libraries.push.impl.store.PushDataStore
import com.echo.tests.testutils.FakeLifecycleOwner
import com.echo.tests.testutils.WarmUpRule
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.testWithLifecycleOwner
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class BatteryOptimizationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter(
            pushDataStore = InMemoryPushDataStore(
                initialShouldDisplayBatteryOptimizationBanner = false,
            ),
            batteryOptimization = FakeBatteryOptimization(
                isIgnoringBatteryOptimizationsResult = false,
            ),
        )
        val lifeCycleOwner = FakeLifecycleOwner()
        presenter.testWithLifecycleOwner(lifeCycleOwner) {
            val initialState = awaitItem()
            assertThat(initialState.shouldDisplayBanner).isFalse()
            lifeCycleOwner.givenState(Lifecycle.State.RESUMED)
        }
    }

    @Test
    fun `present - should display banner`() = runTest {
        val presenter = createPresenter(
            pushDataStore = InMemoryPushDataStore(
                initialShouldDisplayBatteryOptimizationBanner = true,
            ),
            batteryOptimization = FakeBatteryOptimization(
                isIgnoringBatteryOptimizationsResult = false,
            ),
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            assertThat(initialState.shouldDisplayBanner).isFalse()
            assertThat(awaitItem().shouldDisplayBanner).isTrue()
        }
    }

    @Test
    fun `present - should display banner, but setting already performed`() = runTest {
        val presenter = createPresenter(
            pushDataStore = InMemoryPushDataStore(
                initialShouldDisplayBatteryOptimizationBanner = true,
            ),
            batteryOptimization = FakeBatteryOptimization(
                isIgnoringBatteryOptimizationsResult = true,
            ),
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            assertThat(initialState.shouldDisplayBanner).isFalse()
            assertThat(awaitItem().shouldDisplayBanner).isFalse()
        }
    }

    @Test
    fun `present - should display banner, user dismisses`() = runTest {
        val onOptimizationBannerDismissedResult = lambdaRecorder<Unit> { }
        val presenter = createPresenter(
            pushDataStore = InMemoryPushDataStore(
                initialShouldDisplayBatteryOptimizationBanner = true,
            ),
            batteryOptimization = FakeBatteryOptimization(
                isIgnoringBatteryOptimizationsResult = false,
            ),
            mutableBatteryOptimizationStore = FakeMutableBatteryOptimizationStore(
                onOptimizationBannerDismissedResult = onOptimizationBannerDismissedResult,
            ),
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            assertThat(initialState.shouldDisplayBanner).isFalse()
            val displayedItem = awaitItem()
            assertThat(displayedItem.shouldDisplayBanner).isTrue()
            displayedItem.eventSink(BatteryOptimizationEvent.Dismiss)
            onOptimizationBannerDismissedResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - should display banner, user continue, error case`() = runTest {
        val onOptimizationBannerDismissedResult = lambdaRecorder<Unit> { }
        val requestDisablingBatteryOptimizationResult = lambdaRecorder<Boolean> { false }
        val presenter = createPresenter(
            pushDataStore = InMemoryPushDataStore(
                initialShouldDisplayBatteryOptimizationBanner = true,
            ),
            batteryOptimization = FakeBatteryOptimization(
                isIgnoringBatteryOptimizationsResult = false,
                requestDisablingBatteryOptimizationResult = requestDisablingBatteryOptimizationResult
            ),
            mutableBatteryOptimizationStore = FakeMutableBatteryOptimizationStore(
                onOptimizationBannerDismissedResult = onOptimizationBannerDismissedResult,
            ),
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            assertThat(initialState.shouldDisplayBanner).isFalse()
            val displayedItem = awaitItem()
            assertThat(displayedItem.shouldDisplayBanner).isTrue()
            displayedItem.eventSink(BatteryOptimizationEvent.RequestDisableOptimizations)
            requestDisablingBatteryOptimizationResult.assertions().isCalledOnce()
            onOptimizationBannerDismissedResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - should display banner, user continue, nominal case`() = runTest {
        val requestDisablingBatteryOptimizationResult = lambdaRecorder<Boolean> { true }
        val batteryOptimization = FakeBatteryOptimization(
            isIgnoringBatteryOptimizationsResult = false,
            requestDisablingBatteryOptimizationResult = requestDisablingBatteryOptimizationResult
        )
        val presenter = createPresenter(
            pushDataStore = InMemoryPushDataStore(
                initialShouldDisplayBatteryOptimizationBanner = true,
            ),
            batteryOptimization = batteryOptimization,
            mutableBatteryOptimizationStore = FakeMutableBatteryOptimizationStore(),
        )
        val lifeCycleOwner = FakeLifecycleOwner()
        presenter.testWithLifecycleOwner(lifeCycleOwner) {
            val initialState = awaitItem()
            assertThat(initialState.shouldDisplayBanner).isFalse()
            val displayedItem = awaitItem()
            assertThat(displayedItem.shouldDisplayBanner).isTrue()
            displayedItem.eventSink(BatteryOptimizationEvent.RequestDisableOptimizations)
            requestDisablingBatteryOptimizationResult.assertions().isCalledOnce()
            batteryOptimization.isIgnoringBatteryOptimizationsResult = true
            lifeCycleOwner.givenState(Lifecycle.State.RESUMED)
            assertThat(awaitItem().shouldDisplayBanner).isFalse()
            assertThat(awaitItem().shouldDisplayBanner).isFalse()
        }
    }

    private fun createPresenter(
        pushDataStore: PushDataStore = InMemoryPushDataStore(),
        mutableBatteryOptimizationStore: MutableBatteryOptimizationStore = FakeMutableBatteryOptimizationStore(),
        batteryOptimization: BatteryOptimization = FakeBatteryOptimization(),
    ) = BatteryOptimizationPresenter(
        pushDataStore = pushDataStore,
        mutableBatteryOptimizationStore = mutableBatteryOptimizationStore,
        batteryOptimization = batteryOptimization
    )
}

