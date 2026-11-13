/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.roomaliasresolver.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.echo.features.roomaliasresolver.api.RoomAliasResolverEntryPoint
import com.echo.libraries.architecture.createNode
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class DefaultRoomAliasResolverEntryPoint : RoomAliasResolverEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: RoomAliasResolverEntryPoint.Params,
        callback: RoomAliasResolverEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<RoomAliasResolverNode>(
            buildContext = buildContext,
            plugins = listOf(params, callback),
        )
    }
}
