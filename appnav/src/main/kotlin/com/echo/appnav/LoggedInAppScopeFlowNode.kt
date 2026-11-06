/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(DelicateCoilApi::class)

package com.echo.appnav

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.appnav.di.SessionGraphFactory
import com.echo.libraries.architecture.NodeInputs
import com.echo.libraries.architecture.callback
import com.echo.libraries.architecture.createNode
import com.echo.libraries.architecture.inputs
import com.echo.libraries.di.DependencyInjectionGraphOwner
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrixmedia.api.ImageLoaderHolder
import kotlinx.parcelize.Parcelize

/**
 * `LoggedInAppScopeFlowNode` is a Node responsible to set up the Session graph.
 * [com.echo.libraries.di.SessionScope]. It has only one child: [LoggedInFlowNode].
 * This allow to inject objects with SessionScope in the constructor of [LoggedInFlowNode].
 */
@ContributesNode(AppScope::class)
@AssistedInject
class LoggedInAppScopeFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    sessionGraphFactory: SessionGraphFactory,
    private val imageLoaderHolder: ImageLoaderHolder,
) : ParentNode<LoggedInAppScopeFlowNode.NavTarget>(
    navModel = PermanentNavModel(
        navTargets = setOf(NavTarget),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
), DependencyInjectionGraphOwner {
    interface Callback : Plugin {
        fun navigateToBugReport()
        fun navigateToAddAccount()
        fun navigateToSession(sessionId: SessionId)
    }

    private val callback: Callback = callback()

    @Parcelize
    object NavTarget : Parcelable

    data class Inputs(
        val matrixClient: MatrixClient
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    override val graph = sessionGraphFactory.create(inputs.matrixClient)

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onResume = {
                SingletonImageLoader.setUnsafe(imageLoaderHolder.get(inputs.matrixClient))
            },
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        val callback = object : LoggedInFlowNode.Callback {
            override fun navigateToBugReport() {
                callback.navigateToBugReport()
            }

            override fun navigateToAddAccount() {
                callback.navigateToAddAccount()
            }

            override fun navigateToSession(sessionId: SessionId) {
                callback.navigateToSession(sessionId)
            }
        }
        return createNode<LoggedInFlowNode>(buildContext, listOf(callback))
    }

    suspend fun attachSession(): LoggedInFlowNode = waitForChildAttached()

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = navModel,
            modifier = modifier,
        )
    }
}

