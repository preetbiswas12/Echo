/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.test

import com.echo.features.login.api.LoginIntentResolver
import com.echo.features.login.api.LoginParams
import com.echo.tests.testutils.lambda.lambdaError

class FakeLoginIntentResolver(
    private val parseResult: (String) -> LoginParams? = { lambdaError() }
) : LoginIntentResolver {
    override fun parse(uriString: String): LoginParams? {
        return parseResult(uriString)
    }
}

