/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.classic.loginwithclassic

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import com.echo.features.login.impl.login.LoginModeState
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.matrix.api.core.UserId

@Stable
data class LoginWithClassicState(
    val isElementPro: Boolean,
    val userId: UserId,
    val displayName: String?,
    val avatar: Bitmap?,
    val loginWithClassicAction: AsyncAction<Unit>,
    val loginModeState: LoginModeState,
    val eventSink: (LoginWithClassicEvent) -> Unit,
)

