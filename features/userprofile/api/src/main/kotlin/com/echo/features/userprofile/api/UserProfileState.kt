/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.userprofile.api

import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.designsystem.utils.snackbar.SnackbarMessage
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.user.DisplayedStatus

data class UserProfileState(
    val userId: UserId,
    val userName: String?,
    val avatarUrl: String?,
    val verificationState: UserProfileVerificationState,
    val isBlocked: AsyncData<Boolean>,
    val startDmActionState: AsyncAction<RoomId>,
    val displayConfirmationDialog: ConfirmationDialog?,
    val isCurrentUser: Boolean,
    val dmRoomId: RoomId?,
    val canCall: Boolean,
    val snackbarMessage: SnackbarMessage?,
    val displayedStatus: DisplayedStatus?,
    val eventSink: (UserProfileEvent) -> Unit
) {
    enum class ConfirmationDialog {
        Block,
        Unblock
    }
}

enum class UserProfileVerificationState {
    UNKNOWN,
    VERIFIED,
    UNVERIFIED,
    VERIFICATION_VIOLATION,
}
