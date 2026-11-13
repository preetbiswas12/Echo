/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.startchat.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.echo.features.startchat.api.StartChatEntryPoint
import com.echo.libraries.architecture.createNode
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class DefaultStartChatEntryPoint : StartChatEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: StartChatEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<StartChatFlowNode>(buildContext, listOf(callback))
    }
}
