/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.migration.impl.migrations

import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.FakeMatrixClientProvider
import com.echo.libraries.sessionstorage.test.InMemorySessionStore
import com.echo.libraries.sessionstorage.test.aSessionData
import com.echo.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration09Test {
    @Test
    fun `migration on fresh install does nothing`() = runTest {
        val sessionStore = InMemorySessionStore(initialList = listOf(aSessionData()))
        val getClientLambda = lambdaRecorder<SessionId, Result<MatrixClient>> { Result.success(FakeMatrixClient()) }
        val clientProvider = FakeMatrixClientProvider(getClient = getClientLambda)
        val migration = AppMigration09(sessionStore, clientProvider)
        migration.migrate(isFreshInstall = true)

        getClientLambda.assertions().isNeverCalled()
    }

    @Test
    fun `migration on upgrade should invoke the resetWellKnownConfig method`() = runTest {
        val sessionStore = InMemorySessionStore(initialList = listOf(aSessionData()))
        val resetWellKnownLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val getClientLambda = lambdaRecorder<SessionId, Result<MatrixClient>> {
            Result.success(FakeMatrixClient(resetWellKnownConfigLambda = resetWellKnownLambda))
        }
        val clientProvider = FakeMatrixClientProvider(getClient = getClientLambda)
        val migration = AppMigration09(sessionStore, clientProvider)
        migration.migrate(isFreshInstall = false)

        getClientLambda.assertions().isCalledOnce()
        resetWellKnownLambda.assertions().isCalledOnce()
    }
}

