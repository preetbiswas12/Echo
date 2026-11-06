/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.account

import com.echo.libraries.designsystem.utils.snackbar.SnackbarMessage
import com.echo.libraries.matrix.api.user.MatrixUser

data class PreferencesAccountState(
    val myUser: MatrixUser,
    val showSecureBackup: Boolean,
    val showSecureBackupBadge: Boolean,
    val accountManagementUrl: String?,
    val canReportBug: Boolean,
    val showLinkNewDevice: Boolean,
    val canDeactivateAccount: Boolean,
    val snackbarMessage: SnackbarMessage?,
)

