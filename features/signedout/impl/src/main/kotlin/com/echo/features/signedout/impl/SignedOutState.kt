/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.signedout.impl

import com.echo.libraries.matrix.api.user.MatrixUser

data class SignedOutState(
    val signedOutMatrixUser: MatrixUser,
    val eventSink: (SignedOutEvent) -> Unit,
)
