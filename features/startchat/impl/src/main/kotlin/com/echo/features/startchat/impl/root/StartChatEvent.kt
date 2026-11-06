/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.startchat.impl.root

import com.echo.libraries.matrix.api.user.MatrixUser

sealed interface StartChatEvent {
    data class StartDM(val matrixUser: MatrixUser) : StartChatEvent
    data object CancelStartDM : StartChatEvent
}

