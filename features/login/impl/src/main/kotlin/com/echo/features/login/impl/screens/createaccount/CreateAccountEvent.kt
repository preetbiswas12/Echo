/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.createaccount

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

sealed interface CreateAccountEvent {
    data class SetUsername(val username: String) : CreateAccountEvent
    data class SetPassword(val password: String) : CreateAccountEvent
    data class SetConfirmedPassword(val password: String) : CreateAccountEvent
    data class SetRegistrationCode(val registrationCode: String) : CreateAccountEvent
    data object Submit : CreateAccountEvent
    data object ClearError : CreateAccountEvent
}

@Parcelize
data class CreateAccountFormState(
    val username: String,
    val password: String,
    val confirmedPassword: String,
    val registrationCode: String,
) : Parcelable {
    companion object {
        val Default = CreateAccountFormState("", "", "", "")
    }
}
