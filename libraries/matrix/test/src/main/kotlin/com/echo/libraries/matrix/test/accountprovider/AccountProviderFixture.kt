/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test.accountprovider

import com.echo.libraries.matrix.api.accountprovider.AccountProvider
import com.echo.libraries.matrix.test.AN_ACCOUNT_PROVIDER
import com.echo.libraries.matrix.test.AN_ACCOUNT_PROVIDER_URL

fun anAccountProviderGeneric(
    serverName: String = AN_ACCOUNT_PROVIDER,
) = AccountProvider.Generic(
    serverName = serverName,
)

fun anAccountProviderManaged(
    baseUrl: String = AN_ACCOUNT_PROVIDER_URL,
    serverName: String = AN_ACCOUNT_PROVIDER,
    canUseServerName: Boolean = true,
) = AccountProvider.Managed(
    serverName = serverName,
    baseUrl = baseUrl,
    canUseServerName = canUseServerName,
)
