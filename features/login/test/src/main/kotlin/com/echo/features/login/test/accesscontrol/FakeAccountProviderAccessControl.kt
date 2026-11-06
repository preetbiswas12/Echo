/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.test.accesscontrol

import com.echo.features.login.api.accesscontrol.AccountProviderAccessControl
import com.echo.libraries.matrix.api.accountprovider.AccountProvider
import com.echo.tests.testutils.lambda.lambdaError

class FakeAccountProviderAccessControl(
    private val isAllowedToConnectToAccountProviderResult: (AccountProvider) -> Boolean = { lambdaError() },
) : AccountProviderAccessControl {
    override suspend fun isAllowedToConnectToAccountProvider(accountProvider: AccountProvider): Boolean {
        return isAllowedToConnectToAccountProviderResult(accountProvider)
    }
}

