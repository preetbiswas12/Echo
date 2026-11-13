/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.user.editprofile

import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.ui.media.AvatarAction
import com.echo.libraries.permissions.api.PermissionsState
import kotlinx.collections.immutable.ImmutableList

data class EditUserProfileState(
    val userId: UserId,
    val displayName: String,
    val userAvatarUrl: String?,
    val avatarActions: ImmutableList<AvatarAction>,
    val saveButtonEnabled: Boolean,
    val saveAction: AsyncAction<Unit>,
    val cameraPermissionState: PermissionsState,
    val canChangeDisplayName: Boolean,
    val canChangeAvatarUrl: Boolean,
    val eventSink: (EditUserProfileEvent) -> Unit
)
