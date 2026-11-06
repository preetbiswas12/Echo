/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.architecture.createNode
import com.echo.libraries.mediaviewer.api.MediaGalleryEntryPoint
import com.echo.libraries.mediaviewer.impl.gallery.root.MediaGalleryFlowNode

@ContributesBinding(AppScope::class)
class DefaultMediaGalleryEntryPoint : MediaGalleryEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: MediaGalleryEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<MediaGalleryFlowNode>(
            buildContext = buildContext,
            plugins = listOf(callback),
        )
    }
}

