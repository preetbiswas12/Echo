/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.enterprise.impl

import com.echo.features.enterprise.api.ClientBuilderEnterpriseHook
import com.echo.libraries.matrix.api.MatrixClientBuilder
import com.echo.libraries.matrix.api.core.SessionId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

/**
 * Default implementation of [ClientBuilderEnterpriseHook] that does nothing for FOSS.
 */
@ContributesBinding(AppScope::class)
class DefaultClientBuilderEnterpriseHook : ClientBuilderEnterpriseHook {
    override suspend fun beforeClientCreation(clientBuilder: MatrixClientBuilder): MatrixClientBuilder {
        // No modification
        return clientBuilder
    }

    override suspend fun beforeClientCreationWithSession(clientBuilder: MatrixClientBuilder, sessionId: SessionId): MatrixClientBuilder {
        // No modification
        return clientBuilder
    }
}
