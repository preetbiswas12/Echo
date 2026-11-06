/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.securityandprivacy.impl.editroomaddress

import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.matrix.ui.room.address.RoomAddressValidity

data class EditRoomAddressState(
    val homeserverName: String,
    val roomAddress: String,
    val roomAddressValidity: RoomAddressValidity,
    val saveAction: AsyncAction<Unit>,
    val eventSink: (EditRoomAddressEvent) -> Unit
) {
    val canBeSaved = roomAddressValidity == RoomAddressValidity.Valid
}

