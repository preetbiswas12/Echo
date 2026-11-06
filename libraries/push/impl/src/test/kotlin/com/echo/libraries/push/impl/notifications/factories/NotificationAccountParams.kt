/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.notifications.factories

import androidx.annotation.ColorInt
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.matrix.test.A_COLOR_INT
import com.echo.libraries.matrix.ui.components.aMatrixUser

fun aNotificationAccountParams(
    user: MatrixUser = aMatrixUser(),
    @ColorInt color: Int = A_COLOR_INT,
    showSessionId: Boolean = false,
) = NotificationAccountParams(
    user = user,
    color = color,
    showSessionId = showSessionId,
)

