/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.ftue.impl.sessionverification.choosemode

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.features.logout.api.direct.aDirectLogoutState
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.matrix.ui.components.aMatrixUser

class ChooseSelfVerificationModeStatePreviewParam :
    PreviewParameterProvider<ChooseSelfVerificationModeState> {
    override val values = sequenceOf(
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Success(
                aButtonsState(canUseAnotherDevice = false, canUseRecoveryKey = true),
            ),
        ),
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Success(
                aButtonsState(canUseAnotherDevice = false, canUseRecoveryKey = false),
            ),
        ),
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Success(
                aButtonsState(canUseAnotherDevice = true, canUseRecoveryKey = true),
            ),
        ),
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Success(
                aButtonsState(canUseAnotherDevice = true, canUseRecoveryKey = false),
            ),
        ),
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Loading(),
        ),
    )
}

fun aChooseSelfVerificationModeState(
    buttonsState: AsyncData<ChooseSelfVerificationModeState.ButtonsState> = AsyncData.Success(aButtonsState()),
    currentUser: MatrixUser = aMatrixUser(),
    eventSink: (ChooseSelfVerificationModeEvent) -> Unit = {},
) = ChooseSelfVerificationModeState(
    currentUser = currentUser,
    buttonsState = buttonsState,
    directLogoutState = aDirectLogoutState(),
    eventSink = eventSink,
)

fun aButtonsState(
    canUseAnotherDevice: Boolean = true,
    canUseRecoveryKey: Boolean = true,
) = ChooseSelfVerificationModeState.ButtonsState(
    canUseAnotherDevice = canUseAnotherDevice,
    canUseRecoveryKey = canUseRecoveryKey,
)
