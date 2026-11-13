/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.migration.impl.migrations

import com.echo.libraries.preferences.test.FakeSessionPreferencesStoreFactory
import com.echo.libraries.preferences.test.InMemorySessionPreferencesStore
import com.echo.libraries.sessionstorage.test.InMemorySessionStore
import com.echo.libraries.sessionstorage.test.aSessionData
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration02Test {
    @Test
    fun `test migration`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(aSessionData()),
        )
        val sessionPreferencesStore = InMemorySessionPreferencesStore(isSessionVerificationSkipped = false)
        val sessionPreferencesStoreFactory = FakeSessionPreferencesStoreFactory(
            getLambda = lambdaRecorder { _, _ -> sessionPreferencesStore },
            removeLambda = lambdaRecorder { _ -> }
        )
        val migration = AppMigration02(sessionStore = sessionStore, sessionPreferenceStoreFactory = sessionPreferencesStoreFactory)

        migration.migrate(true)

        // We got the session preferences store
        sessionPreferencesStoreFactory.getLambda.assertions().isCalledOnce()
        // We changed the settings for the skipping the session verification
        assertThat(sessionPreferencesStore.isSessionVerificationSkipped().first()).isTrue()
        // We removed the session preferences store from cache
        sessionPreferencesStoreFactory.removeLambda.assertions().isCalledOnce()
    }
}
