/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test

import com.echo.libraries.matrix.api.ClientUrlContentFetcher
import com.echo.tests.testutils.lambda.lambdaError

class FakeClientUrlContentFetcher(
    override val server: String = A_SERVER_NAME,
    override val homeserverUrl: String = A_HOMESERVER_URL,
    private val getUrlResult: (String) -> Result<ByteArray> = { lambdaError() },
) : ClientUrlContentFetcher {
    override suspend fun getUrl(url: String): Result<ByteArray> {
        return getUrlResult(url)
    }
}
