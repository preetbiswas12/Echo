/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.leaveroom.impl

import com.echo.features.leaveroom.api.LeaveRoomEvent

sealed interface InternalLeaveRoomEvent : LeaveRoomEvent {
    data object ResetState : InternalLeaveRoomEvent
}

