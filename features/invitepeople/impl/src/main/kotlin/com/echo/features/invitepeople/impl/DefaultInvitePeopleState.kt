/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invitepeople.impl

import androidx.compose.foundation.text.input.TextFieldState
import com.echo.features.invitepeople.api.InvitePeopleEvent
import com.echo.features.invitepeople.api.InvitePeopleState
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.designsystem.theme.components.SearchBarResultState
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class DefaultInvitePeopleState(
    val room: AsyncData<Unit>,
    override val canInvite: Boolean,
    val searchQuery: TextFieldState,
    val showSearchLoader: Boolean,
    val searchResults: SearchBarResultState<ImmutableList<InvitableUser>>,
    val selectedUsers: ImmutableList<MatrixUser>,
    override val isSearchActive: Boolean,
    override val sendInvitesAction: AsyncAction<Unit>,
    override val createRoomFromDmAction: AsyncAction<RoomId>,
    val suggestions: ImmutableList<InvitableUser>,
    override val eventSink: (InvitePeopleEvent) -> Unit
) : InvitePeopleState
