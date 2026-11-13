/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.poll.test.create

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.echo.features.poll.api.create.CreatePollEntryPoint
import com.echo.features.poll.api.create.CreatePollEntryPoint.Params
import com.echo.tests.testutils.lambda.lambdaError

class FakeCreatePollEntryPoint : CreatePollEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
    ): Node = lambdaError()
}
