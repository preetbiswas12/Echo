/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.createaccount

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.features.login.impl.accountprovider.anAccountProviderManaged
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.matrix.api.accountprovider.AccountProvider
import com.echo.libraries.matrix.api.auth.RegistrationException
import com.echo.libraries.matrix.api.core.SessionId

open class CreateAccountStatePreviewParam : PreviewParameterProvider<CreateAccountState> {
    override val values: Sequence<CreateAccountState>
        get() = sequenceOf(
            aCreateAccountState(),
            // Loading
            aCreateAccountState(registerAction = AsyncData.Loading()),
            // Error
            aCreateAccountState(registerAction = AsyncData.Failure(RegistrationException("M_USER_IN_USE", null))),
        )
}

fun aCreateAccountState(
    accountProvider: AccountProvider = anAccountProviderManaged(),
    formState: CreateAccountFormState = CreateAccountFormState.Default,
    registerAction: AsyncData<SessionId> = AsyncData.Uninitialized,
    eventSink: (CreateAccountEvent) -> Unit = {},
) = CreateAccountState(
    accountProvider = accountProvider,
    formState = formState,
    registerAction = registerAction,
    eventSink = eventSink,
)
