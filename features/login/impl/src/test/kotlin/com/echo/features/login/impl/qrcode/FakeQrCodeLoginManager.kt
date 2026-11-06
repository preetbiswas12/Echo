/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.qrcode

import com.echo.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import com.echo.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.MutableStateFlow

class FakeQrCodeLoginManager(
    var authenticateResult: (MatrixQrCodeLoginData) -> Result<SessionId> =
        lambdaRecorder<MatrixQrCodeLoginData, Result<SessionId>> { Result.success(A_SESSION_ID) },
    var resetAction: () -> Unit = lambdaRecorder<Unit> { },
) : QrCodeLoginManager {
    override val currentLoginStep: MutableStateFlow<QrCodeLoginStep> =
        MutableStateFlow(QrCodeLoginStep.Uninitialized)

    override suspend fun authenticate(qrCodeLoginData: MatrixQrCodeLoginData): Result<SessionId> {
        return authenticateResult(qrCodeLoginData)
    }

    override fun reset() {
        resetAction()
    }
}

