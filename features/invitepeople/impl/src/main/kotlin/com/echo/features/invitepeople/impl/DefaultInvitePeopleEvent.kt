/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invitepeople.impl

import com.echo.features.invitepeople.api.InvitePeopleEvent
import com.echo.libraries.matrix.api.user.MatrixUser

sealed interface DefaultInvitePeopleEvent : InvitePeopleEvent {
    data class ToggleUser(val user: MatrixUser) : DefaultInvitePeopleEvent
    data class OnSearchActiveChanged(val active: Boolean) : DefaultInvitePeopleEvent
    data object DismissUnknownUsersModal : DefaultInvitePeopleEvent
    data object RemoveUnknownUsers : DefaultInvitePeopleEvent
}

