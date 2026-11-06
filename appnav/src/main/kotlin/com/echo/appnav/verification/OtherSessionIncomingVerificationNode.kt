/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.appnav.verification

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
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
import com.echo.features.verifysession.api.IncomingVerificationEntryPoint
import com.echo.libraries.architecture.NodeInputs
import com.echo.libraries.architecture.callback
import com.echo.libraries.architecture.inputs
import com.echo.libraries.di.DependencyInjectionGraphOwner
import com.echo.libraries.matrix.api.MatrixClientProvider
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.verification.VerificationRequest
import kotlinx.parcelize.Parcelize

/**
 * Display an incoming verification request received by a session which is not the current one, without switching the current session.
 * It sets up the Session graph of the session receiving the request.
 */
@ContributesNode(AppScope::class)
@AssistedInject
class OtherSessionIncomingVerificationNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    sessionGraphFactory: SessionGraphFactory,
    matrixClientProvider: MatrixClientProvider,
    private val incomingVerificationEntryPoint: IncomingVerificationEntryPoint,
) : ParentNode<OtherSessionIncomingVerificationNode.NavTarget>(
    navModel = PermanentNavModel(
        navTargets = setOf(NavTarget),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
), DependencyInjectionGraphOwner {
    interface Callback : Plugin {
        fun onDone()
    }

    @Parcelize
    object NavTarget : Parcelable

    data class Inputs(
        val sessionId: SessionId,
        val verificationRequest: VerificationRequest.Incoming,
    ) : NodeInputs

    private val callback: Callback = callback()
    private val inputs: Inputs = inputs()
    override val graph = sessionGraphFactory.create(
        requireNotNull(matrixClientProvider.getOrNull(inputs.sessionId)) { "No MatrixClient for session ${inputs.sessionId}" }
    )

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return incomingVerificationEntryPoint.createNode(
            parentNode = this,
            buildContext = buildContext,
            params = IncomingVerificationEntryPoint.Params(inputs.verificationRequest),
            callback = object : IncomingVerificationEntryPoint.Callback {
                override fun onDone() {
                    callback.onDone()
                }
            },
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = navModel,
            modifier = modifier,
        )
    }
}

