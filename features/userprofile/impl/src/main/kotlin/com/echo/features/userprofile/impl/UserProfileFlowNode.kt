/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.userprofile.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.features.call.api.CallData
import com.echo.features.call.api.ElementCallEntryPoint
import com.echo.features.userprofile.api.UserProfileEntryPoint
import com.echo.features.userprofile.impl.root.UserProfileNode
import com.echo.features.userprofile.shared.UserProfileNodeHelper
import com.echo.features.verifysession.api.OutgoingVerificationEntryPoint
import com.echo.libraries.architecture.BackstackView
import com.echo.libraries.architecture.BaseFlowNode
import com.echo.libraries.architecture.callback
import com.echo.libraries.architecture.createNode
import com.echo.libraries.architecture.inputs
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.notification.CallIntent
import com.echo.libraries.matrix.api.verification.VerificationRequest
import com.echo.libraries.mediaviewer.api.MediaViewerEntryPoint
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class UserProfileFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val sessionId: SessionId,
    private val mediaViewerEntryPoint: MediaViewerEntryPoint,
    private val outgoingVerificationEntryPoint: OutgoingVerificationEntryPoint,
) : BaseFlowNode<UserProfileFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class AvatarPreview(val name: String, val avatarUrl: String) : NavTarget

        @Parcelize
        data class VerifyUser(val userId: UserId) : NavTarget
    }

    private val callback: UserProfileEntryPoint.Callback = callback()
    private val inputs = inputs<UserProfileEntryPoint.Params>()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : UserProfileNodeHelper.Callback {
                    override fun navigateToAvatarPreview(username: String, avatarUrl: String) {
                        backstack.push(NavTarget.AvatarPreview(username, avatarUrl))
                    }

                    override fun navigateToRoom(roomId: RoomId) {
                        callback.navigateToRoom(roomId)
                    }

                    override fun startCall(dmRoomId: RoomId, callIntent: CallIntent) {
                        elementCallEntryPoint.startCall(
                            CallData(
                                sessionId = sessionId,
                                roomId = dmRoomId,
                                isAudioCall = callIntent == CallIntent.AUDIO
                            )
                        )
                    }

                    override fun startVerifyUserFlow(userId: UserId) {
                        backstack.push(NavTarget.VerifyUser(userId))
                    }
                }
                val params = UserProfileNode.UserProfileInputs(userId = inputs.userId)
                createNode<UserProfileNode>(buildContext, listOf(callback, params))
            }
            is NavTarget.AvatarPreview -> {
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }

                    override fun viewInTimeline(eventId: EventId) {
                        // Cannot happen
                    }

                    override fun forwardEvent(eventId: EventId) {
                        // Cannot happen
                    }
                }
                val params = mediaViewerEntryPoint.createParamsForAvatar(
                    filename = navTarget.name,
                    avatarUrl = navTarget.avatarUrl,
                )
                mediaViewerEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback,
                )
            }
            is NavTarget.VerifyUser -> {
                val params = OutgoingVerificationEntryPoint.Params(
                    showDeviceVerifiedScreen = false,
                    verificationRequest = VerificationRequest.Outgoing.User(userId = navTarget.userId)
                )
                outgoingVerificationEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = object : OutgoingVerificationEntryPoint.Callback {
                        override fun navigateToLearnMoreAboutEncryption() {
                            // No op
                        }

                        override fun onBack() {
                            // No op
                        }

                        override fun onDone() {
                            // No op
                        }
                    }
                )
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}

