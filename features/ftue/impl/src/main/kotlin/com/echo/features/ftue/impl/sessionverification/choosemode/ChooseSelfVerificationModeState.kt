/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.ftue.impl.sessionverification.choosemode

import com.echo.features.logout.api.direct.DirectLogoutState
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.matrix.api.user.MatrixUser

data class ChooseSelfVerificationModeState(
    val currentUser: MatrixUser,
    val buttonsState: AsyncData<ButtonsState>,
    val directLogoutState: DirectLogoutState,
    val eventSink: (ChooseSelfVerificationModeEvent) -> Unit,
) {
    data class ButtonsState(
        val canUseAnotherDevice: Boolean,
        val canUseRecoveryKey: Boolean,
    )
}

