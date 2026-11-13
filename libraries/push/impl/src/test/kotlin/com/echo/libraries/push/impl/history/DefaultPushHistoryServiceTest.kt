/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.history

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.echo.libraries.matrix.test.AN_EVENT_ID
import com.echo.libraries.matrix.test.AN_EVENT_ID_2
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.matrix.test.A_SESSION_ID_2
import com.echo.libraries.push.impl.PushDatabase
import com.echo.libraries.push.impl.notifications.fixtures.aPushRequest
import com.echo.libraries.push.impl.push.PushRequestStatus
import com.echo.services.toolbox.test.systemclock.FakeSystemClock
import com.echo.tests.testutils.robolectric.RobolectricTest
import com.echo.tests.testutils.testCoroutineDispatchers
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import kotlin.time.Instant

class DefaultPushHistoryServiceTest : RobolectricTest() {
    @Test
    fun `getPendingPushRequestsFlow emits the pending requests of the session received after the given date`() = runTest {
        val sut = createDefaultPushHistoryService()
        // Ignored: received at the given date, for another session, or not pending
        sut.insertOrUpdatePushRequest(aPushRequest(eventId = AN_EVENT_ID).copy(pushDate = 1_000))
        sut.insertOrUpdatePushRequest(aPushRequest(sessionId = A_SESSION_ID_2).copy(pushDate = 2_000))
        sut.insertOrUpdatePushRequest(aPushRequest(eventId = AN_EVENT_ID_2, status = PushRequestStatus.SUCCESS).copy(pushDate = 2_000))

        sut.getPendingPushRequestsFlow(A_SESSION_ID, since = Instant.fromEpochMilliseconds(1_000)).test {
            assertThat(awaitItem()).isEmpty()

            val newerRequest = aPushRequest(eventId = AN_EVENT_ID_2).copy(pushDate = 3_000)
            sut.insertOrUpdatePushRequest(newerRequest)

            assertThat(awaitItem()).containsExactly(newerRequest)
        }
    }

    private fun TestScope.createDefaultPushHistoryService(): DefaultPushHistoryService {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PushDatabase.Schema.create(driver)
        return DefaultPushHistoryService(
            pushDatabase = PushDatabase(driver),
            systemClock = FakeSystemClock(),
            dispatchers = testCoroutineDispatchers(),
            context = RuntimeEnvironment.getApplication(),
        )
    }
}
