/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.call.api

import android.os.Parcelable
import com.echo.libraries.architecture.NodeInputs
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallData(
    val sessionId: SessionId,
    val roomId: RoomId,
    val isAudioCall: Boolean
) : NodeInputs, Parcelable

