/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.createaccount

import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.matrix.api.accountprovider.AccountProvider
import com.echo.libraries.matrix.api.core.SessionId

/**
 * The valid characters for the local part of a Matrix user ID, as enforced by most homeservers.
 */
private val USERNAME_REGEX = Regex("^[a-z0-9._=/+-]+$")

data class CreateAccountState(
    val accountProvider: AccountProvider,
    val formState: CreateAccountFormState,
    val registerAction: AsyncData<SessionId>,
    val eventSink: (CreateAccountEvent) -> Unit,
) {
    /** Whether the username only contains characters allowed in the local part of a Matrix user ID. */
    val isUsernameValid: Boolean
        get() = formState.username.isNotEmpty() && USERNAME_REGEX.matches(formState.username)

    val isPasswordValid: Boolean
        get() = formState.password.length >= MIN_PASSWORD_LENGTH

    val isPasswordConfirmed: Boolean
        get() = formState.password == formState.confirmedPassword

    val submitEnabled: Boolean
        get() = registerAction !is AsyncData.Failure &&
            isUsernameValid &&
            isPasswordValid &&
            isPasswordConfirmed

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
