/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.space.impl.addroom

import androidx.compose.foundation.text.input.TextFieldState
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.designsystem.theme.components.SearchBarResultState
import com.echo.libraries.matrix.ui.model.SelectRoomInfo
import kotlinx.collections.immutable.ImmutableList

data class AddRoomToSpaceState(
    val searchQuery: TextFieldState,
    val isSearchActive: Boolean,
    val searchResults: SearchBarResultState<ImmutableList<SelectRoomInfo>>,
    val selectedRooms: ImmutableList<SelectRoomInfo>,
    val suggestions: ImmutableList<SelectRoomInfo>,
    val saveAction: AsyncAction<Unit>,
    val eventSink: (AddRoomToSpaceEvent) -> Unit,
) {
    val canSave: Boolean = selectedRooms.isNotEmpty() && !saveAction.isLoading()
}
