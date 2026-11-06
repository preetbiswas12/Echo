/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.services.toolbox.test.intent

import android.content.Intent
import com.echo.services.toolbox.api.intent.ExternalIntentLauncher
import com.echo.tests.testutils.lambda.lambdaError

class FakeExternalIntentLauncher(
    var launchLambda: (Intent) -> Unit = { lambdaError() },
) : ExternalIntentLauncher {
    override fun launch(intent: Intent) {
        launchLambda(intent)
    }
}

