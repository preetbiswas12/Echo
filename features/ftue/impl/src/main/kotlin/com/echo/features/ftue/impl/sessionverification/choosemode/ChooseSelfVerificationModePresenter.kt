/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.ftue.impl.sessionverification.choosemode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import com.echo.features.logout.api.direct.DirectLogoutEvent
import com.echo.features.logout.api.direct.DirectLogoutState
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.core.coroutine.mapState
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.encryption.EncryptionService
import com.echo.libraries.matrix.api.encryption.RecoveryState

@Inject
class ChooseSelfVerificationModePresenter(
    private val matrixClient: MatrixClient,
    private val encryptionService: EncryptionService,
    private val directLogoutPresenter: Presenter<DirectLogoutState>,
) : Presenter<ChooseSelfVerificationModeState> {
    @Composable
    override fun present(): ChooseSelfVerificationModeState {
        val currentUser by matrixClient.userProfile.collectAsState()
        val hasDevicesToVerifyAgainst by encryptionService.hasDevicesToVerifyAgainst.collectAsState()
        val canUseRecoveryKey by produceState<AsyncData<Boolean>>(AsyncData.Uninitialized) {
            encryptionService.recoveryStateStateFlow
                .mapState { recoveryState ->
                    when (recoveryState) {
                        RecoveryState.WAITING_FOR_SYNC,
                        RecoveryState.UNKNOWN -> AsyncData.Loading()
                        RecoveryState.INCOMPLETE -> AsyncData.Success(true)
                        RecoveryState.ENABLED,
                        RecoveryState.DISABLED -> AsyncData.Success(false)
                    }
                }
                .collect {
                    value = it
                }
        }
        val buttonsState by remember {
            derivedStateOf {
                val canUseAnotherDevice = hasDevicesToVerifyAgainst.dataOrNull()
                val canUseRecoveryKey = canUseRecoveryKey.dataOrNull()
                if (canUseAnotherDevice == null || canUseRecoveryKey == null) {
                    AsyncData.Loading()
                } else {
                    AsyncData.Success(
                        ChooseSelfVerificationModeState.ButtonsState(
                            canUseAnotherDevice = canUseAnotherDevice,
                            canUseRecoveryKey = canUseRecoveryKey,
                        )
                    )
                }
            }
        }

        val directLogoutState = directLogoutPresenter.present()

        fun handleEvent(event: ChooseSelfVerificationModeEvent) {
            when (event) {
                ChooseSelfVerificationModeEvent.SignOut -> directLogoutState.eventSink(DirectLogoutEvent.Logout(ignoreSdkError = false))
            }
        }

        return ChooseSelfVerificationModeState(
            currentUser = currentUser,
            buttonsState = buttonsState,
            directLogoutState = directLogoutState,
            eventSink = ::handleEvent,
        )
    }
}

