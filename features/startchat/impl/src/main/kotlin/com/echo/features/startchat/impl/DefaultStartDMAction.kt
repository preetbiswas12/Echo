/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.startchat.impl

import androidx.compose.runtime.MutableState
import com.echo.features.enterprise.api.SessionEnterpriseService
import com.echo.features.startchat.api.ConfirmingStartDmWithMatrixUser
import com.echo.features.startchat.api.StartDMAction
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.room.StartDMResult
import com.echo.libraries.matrix.api.room.startDM
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.services.analytics.api.AnalyticsService
import dev.zacsweers.metro.ContributesBinding
import im.vector.app.features.analytics.plan.CreatedRoom

@ContributesBinding(SessionScope::class)
class DefaultStartDMAction(
    private val matrixClient: MatrixClient,
    private val analyticsService: AnalyticsService,
    private val sessionEnterpriseService: SessionEnterpriseService,
) : StartDMAction {
    override suspend fun execute(
        matrixUser: MatrixUser,
        createIfDmDoesNotExist: Boolean,
        actionState: MutableState<AsyncAction<RoomId>>,
    ) {
        actionState.value = AsyncAction.Loading
        when (val result = matrixClient.startDM(
            userId = matrixUser.userId,
            createIfDmDoesNotExist = createIfDmDoesNotExist,
            isEncryped = !sessionEnterpriseService.isEncryptionDisabledByHomeserver(),
        )) {
            is StartDMResult.Success -> {
                if (result.isNew) {
                    analyticsService.capture(CreatedRoom(isDM = true))
                }
                actionState.value = AsyncAction.Success(result.roomId)
            }
            is StartDMResult.Failure -> {
                actionState.value = AsyncAction.Failure(result.throwable)
            }
            StartDMResult.DmDoesNotExist -> {
                val identityState = matrixClient.encryptionService.getUserIdentity(matrixUser.userId, fallbackToServer = false).getOrNull()
                actionState.value = ConfirmingStartDmWithMatrixUser(
                    matrixUser = matrixUser,
                    isUserIdentityUnknown = identityState == null
                )
            }
        }
    }
}
