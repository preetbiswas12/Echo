/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.verifysession.impl.outgoing

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import com.echo.features.verifysession.api.OutgoingVerificationEntryPoint
import com.echo.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultOutgoingVerificationEntryPoint : OutgoingVerificationEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: OutgoingVerificationEntryPoint.Params,
        callback: OutgoingVerificationEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<OutgoingVerificationNode>(buildContext, listOf(params, callback))
    }
}

