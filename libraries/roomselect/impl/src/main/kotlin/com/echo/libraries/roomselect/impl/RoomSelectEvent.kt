/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.roomselect.impl

import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.ui.model.SelectRoomInfo

sealed interface RoomSelectEvent {
    data class ToggleSelectedRoom(val room: SelectRoomInfo) : RoomSelectEvent
    data class UpdateVisibleRange(val range: IntRange) : RoomSelectEvent
    data object ToggleAccountListExpanded : RoomSelectEvent
    data class SelectAccount(val sessionId: SessionId) : RoomSelectEvent
}
