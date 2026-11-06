/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invite.impl.fake

import com.echo.features.invite.impl.AcceptInvite
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask

class FakeAcceptInvite(
    private val lambda: (RoomId) -> Result<RoomId> = { lambdaError() },
) : AcceptInvite {
    override suspend fun invoke(roomId: RoomId) = simulateLongTask {
        lambda(roomId)
    }
}

