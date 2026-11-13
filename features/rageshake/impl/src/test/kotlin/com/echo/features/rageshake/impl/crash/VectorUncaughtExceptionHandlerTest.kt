/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.rageshake.impl.crash

import com.echo.libraries.matrix.test.AN_EXCEPTION
import com.echo.libraries.preferences.test.FakePreferenceDataStoreFactory
import com.echo.tests.testutils.robolectric.RobolectricTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class VectorUncaughtExceptionHandlerTest : RobolectricTest() {
    @Test
    fun `activate should change the default handler`() {
        val sut = VectorUncaughtExceptionHandler(PreferencesCrashDataStore(FakePreferenceDataStoreFactory()))
        sut.activate()
        assertThat(Thread.getDefaultUncaughtExceptionHandler()).isInstanceOf(VectorUncaughtExceptionHandler::class.java)
    }

    @Test
    fun `uncaught exception`() = runTest {
        val crashDataStore = PreferencesCrashDataStore(FakePreferenceDataStoreFactory())
        assertThat(crashDataStore.appHasCrashed().first()).isFalse()
        assertThat(crashDataStore.crashInfo().first()).isEmpty()
        val sut = VectorUncaughtExceptionHandler(crashDataStore)
        sut.uncaughtException(Thread(), AN_EXCEPTION)
        assertThat(crashDataStore.appHasCrashed().first()).isTrue()
        val crashInfo = crashDataStore.crashInfo().first()
        assertThat(crashInfo).isNotEmpty()
        assertThat(crashInfo).contains("Memory statuses")
        crashDataStore.resetAppHasCrashed()
        assertThat(crashDataStore.appHasCrashed().first()).isFalse()
    }
}
