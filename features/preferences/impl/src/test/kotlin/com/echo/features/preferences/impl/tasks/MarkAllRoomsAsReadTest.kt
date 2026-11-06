/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.tasks

import com.google.common.truth.Truth.assertThat
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.push.test.notifications.FakeNotificationCleaner
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import com.echo.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MarkAllRoomsAsReadTest {
    @Test
    fun `invoke - delegates to client and clears all notifications`() = runTest {
        val markAllRoomsAsReadLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val clearAllMessagesEventsLambda = lambdaRecorder<SessionId, Unit> { }
        val markAllRoomsAsRead = DefaultMarkAllRoomsAsRead(
            client = FakeMatrixClient(
                markAllRoomsAsReadResult = markAllRoomsAsReadLambda,
            ),
            notificationCleaner = FakeNotificationCleaner(
                clearAllMessagesEventsLambda = clearAllMessagesEventsLambda,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        val result = markAllRoomsAsRead()

        assertThat(result.isSuccess).isTrue()
        markAllRoomsAsReadLambda.assertions().isCalledOnce()
        clearAllMessagesEventsLambda.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @Test
    fun `invoke - does not clear notifications when client fails`() = runTest {
        val clearAllMessagesEventsLambda = lambdaRecorder<SessionId, Unit> { }
        val markAllRoomsAsRead = DefaultMarkAllRoomsAsRead(
            client = FakeMatrixClient(
                markAllRoomsAsReadResult = { Result.failure(IllegalStateException("Failed")) },
            ),
            notificationCleaner = FakeNotificationCleaner(
                clearAllMessagesEventsLambda = clearAllMessagesEventsLambda,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        val result = markAllRoomsAsRead()

        assertThat(result.isFailure).isTrue()
        clearAllMessagesEventsLambda.assertions().isNeverCalled()
    }
}

