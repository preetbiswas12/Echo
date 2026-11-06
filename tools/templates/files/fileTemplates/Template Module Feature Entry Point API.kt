package com.echo.features.${MODULE_NAME}.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.echo.libraries.architecture.FeatureEntryPoint

interface ${FEATURE_NAME}EntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        // Add your callbacks
    }
}

