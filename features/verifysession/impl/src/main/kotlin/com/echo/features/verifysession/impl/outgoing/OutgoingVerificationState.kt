/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.verifysession.impl.outgoing

import androidx.compose.runtime.Stable
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.matrix.api.verification.SessionVerificationData
import com.echo.libraries.matrix.api.verification.VerificationRequest

data class OutgoingVerificationState(
    val step: Step,
    val request: VerificationRequest.Outgoing,
    val eventSink: (OutgoingVerificationViewEvent) -> Unit,
) {
    @Stable
    sealed interface Step {
        data object Loading : Step
        data object Initial : Step
        data object Canceled : Step
        data object AwaitingOtherDeviceResponse : Step
        data object Ready : Step
        data class Verifying(val data: SessionVerificationData, val state: AsyncData<Unit>) : Step
        data object Completed : Step
        data object Exit : Step

        val isTimeLimited: Boolean
            get() = this is Initial ||
                this is AwaitingOtherDeviceResponse ||
                this is Ready ||
                this is Verifying
    }
}
