/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.lockscreen.impl.unlock

import com.echo.features.lockscreen.impl.biometric.BiometricAuthenticator
import com.echo.features.lockscreen.impl.biometric.BiometricUnlockError
import com.echo.features.lockscreen.impl.pin.model.PinEntry
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.architecture.AsyncData

data class PinUnlockState(
    val canNavigateBack: Boolean,
    val pinEntry: AsyncData<PinEntry>,
    val showWrongPinTitle: Boolean,
    val remainingAttempts: AsyncData<Int>,
    val showSignOutPrompt: Boolean,
    val signOutAction: AsyncAction<Unit>,
    val showBiometricUnlock: Boolean,
    val isUnlocked: Boolean,
    val biometricUnlockResult: BiometricAuthenticator.AuthenticationResult?,
    val eventSink: (PinUnlockEvent) -> Unit
) {
    val isSignOutPromptCancellable = if (pinEntry.isFailure()) {
        false
    } else {
        when (remainingAttempts) {
            is AsyncData.Success -> remainingAttempts.data > 0
            else -> true
        }
    }

    val biometricUnlockErrorMessage = when {
        biometricUnlockResult is BiometricAuthenticator.AuthenticationResult.Failure &&
            biometricUnlockResult.error is BiometricUnlockError &&
            biometricUnlockResult.error.isAuthDisabledError -> {
            biometricUnlockResult.error.message
        }
        else -> null
    }
    val showBiometricUnlockError = biometricUnlockErrorMessage != null
}
