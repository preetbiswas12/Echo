/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.classic.loginwithclassic

import android.graphics.Bitmap
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.features.login.impl.login.LoginModeState
import com.echo.features.login.impl.login.aLoginModeState
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.designsystem.preview.USER_NAME_ALICE
import com.echo.libraries.matrix.api.core.UserId

open class LoginWithClassicStatePreviewParam : PreviewParameterProvider<LoginWithClassicState> {
    override val values: Sequence<LoginWithClassicState>
        get() = sequenceOf(
            aLoginWithClassicState(),
            aLoginWithClassicState(isElementPro = true, displayName = USER_NAME_ALICE),
        )
}

fun aLoginWithClassicState(
    isElementPro: Boolean = false,
    userId: UserId = UserId("@alice:matrix.org"),
    displayName: String? = null,
    avatar: Bitmap? = null,
    loginWithClassicAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    loginModeState: LoginModeState = aLoginModeState(),
    eventSink: (LoginWithClassicEvent) -> Unit = {},
) = LoginWithClassicState(
    isElementPro = isElementPro,
    userId = userId,
    displayName = displayName,
    avatar = avatar,
    loginWithClassicAction = loginWithClassicAction,
    loginModeState = loginModeState,
    eventSink = eventSink,
)

