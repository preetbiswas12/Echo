/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test

import com.echo.libraries.matrix.api.TemporaryMatrixClient
import com.echo.libraries.matrix.api.TemporaryMatrixClientFactory

class FakeTemporaryMatrixClientFactory(
    private val createResult: (String) -> Result<TemporaryMatrixClient> = { Result.success(FakeTemporaryMatrixClient()) },
) : TemporaryMatrixClientFactory {
    override suspend fun create(serverName: String): Result<TemporaryMatrixClient> = createResult(serverName)
}

