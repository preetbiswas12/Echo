/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.enterprise.test

import com.echo.features.enterprise.api.ClientBuilderEnterpriseHook
import com.echo.libraries.matrix.api.MatrixClientBuilder
import com.echo.libraries.matrix.api.core.SessionId

class FakeClientBuilderEnterpriseHook(
    private val beforeClientCreationResult: (MatrixClientBuilder) -> MatrixClientBuilder = { it },
    private val beforeClientCreationWithSessionResult: (MatrixClientBuilder, SessionId) -> MatrixClientBuilder = { clientBuilder, _ -> clientBuilder },
) : ClientBuilderEnterpriseHook {
    override suspend fun beforeClientCreation(clientBuilder: MatrixClientBuilder): MatrixClientBuilder {
        return beforeClientCreationResult(clientBuilder)
    }

    override suspend fun beforeClientCreationWithSession(clientBuilder: MatrixClientBuilder, sessionId: SessionId): MatrixClientBuilder {
        return beforeClientCreationWithSessionResult(clientBuilder, sessionId)
    }
}
