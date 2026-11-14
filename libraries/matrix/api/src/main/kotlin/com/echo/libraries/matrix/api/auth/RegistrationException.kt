/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.api.auth

/**
 * The homeserver rejected an account creation request.
 *
 * @param errcode the Matrix error code returned by the server, e.g. `M_USER_IN_USE`, or `null` when the
 * failure happened before the server could answer.
 * @param message the human-readable error from the server, when any.
 */
class RegistrationException(
    val errcode: String?,
    message: String?,
) : Exception(message ?: errcode ?: "Account creation failed")
