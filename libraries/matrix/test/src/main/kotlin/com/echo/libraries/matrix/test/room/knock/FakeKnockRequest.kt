/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test.room.knock

import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.room.knock.KnockRequest
import com.echo.libraries.matrix.test.AN_AVATAR_URL
import com.echo.libraries.matrix.test.AN_EVENT_ID
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.A_USER_NAME
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask

class FakeKnockRequest(
    override val eventId: EventId = AN_EVENT_ID,
    override val userId: UserId = A_USER_ID,
    override val displayName: String? = A_USER_NAME,
    override val avatarUrl: String? = AN_AVATAR_URL,
    override val reason: String? = null,
    override val timestamp: Long? = null,
    override val isSeen: Boolean = false,
    val acceptLambda: () -> Result<Unit> = { lambdaError() },
    val declineLambda: (String?) -> Result<Unit> = { lambdaError() },
    val declineAndBanLambda: (String?) -> Result<Unit> = { lambdaError() },
    val markAsSeenLambda: () -> Result<Unit> = { lambdaError() },
) : KnockRequest {
    override suspend fun accept(): Result<Unit> = simulateLongTask {
        acceptLambda()
    }

    override suspend fun decline(reason: String?): Result<Unit> = simulateLongTask {
        declineLambda(reason)
    }

    override suspend fun declineAndBan(reason: String?): Result<Unit> = simulateLongTask {
        declineAndBanLambda(reason)
    }

    override suspend fun markAsSeen(): Result<Unit> = simulateLongTask {
        markAsSeenLambda()
    }
}
