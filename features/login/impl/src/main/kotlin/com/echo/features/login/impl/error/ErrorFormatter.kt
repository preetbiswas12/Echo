/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.error

import androidx.annotation.StringRes
import com.echo.features.login.impl.R
import com.echo.libraries.matrix.api.auth.AuthErrorCode
import com.echo.libraries.matrix.api.auth.AuthenticationException
import com.echo.libraries.matrix.api.auth.RegistrationException
import com.echo.libraries.matrix.api.auth.errorCode
import com.echo.libraries.ui.strings.CommonStrings

@StringRes
fun loginError(
    throwable: Throwable
): Int {
    val authException = throwable as? AuthenticationException ?: return CommonStrings.error_unknown
    return when (authException.errorCode) {
        AuthErrorCode.FORBIDDEN -> R.string.screen_login_error_invalid_credentials
        AuthErrorCode.USER_DEACTIVATED -> R.string.screen_login_error_deactivated_account
        AuthErrorCode.UNKNOWN -> CommonStrings.error_unknown
    }
}

@StringRes
fun registrationError(
    throwable: Throwable,
): Int {
    val registrationException = throwable as? RegistrationException ?: return CommonStrings.error_unknown
    return when (registrationException.errcode) {
        "M_USER_IN_USE" -> R.string.screen_create_account_error_username_taken
        "M_INVALID_USERNAME" -> R.string.screen_create_account_error_invalid_username
        "M_FORBIDDEN" -> R.string.screen_create_account_error_invalid_registration_code
        else -> CommonStrings.error_unknown
    }
}
