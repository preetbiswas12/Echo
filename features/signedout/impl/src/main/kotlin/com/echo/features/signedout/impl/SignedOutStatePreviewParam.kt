/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.signedout.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.matrix.ui.components.aMatrixUser

open class SignedOutStatePreviewParam : PreviewParameterProvider<SignedOutState> {
    override val values: Sequence<SignedOutState>
        get() = sequenceOf(
            aSignedOutState(),
            aSignedOutState(signedOutSession = aMatrixUser(id = "@alice:server.org", displayName = null)),
        )
}

internal fun aSignedOutState(
    signedOutSession: MatrixUser = aMatrixUser(id = "@alice:server.org", displayName = "Alice"),
    eventSink: (SignedOutEvent) -> Unit = {},
) = SignedOutState(
    signedOutMatrixUser = signedOutSession,
    eventSink = eventSink,
)

