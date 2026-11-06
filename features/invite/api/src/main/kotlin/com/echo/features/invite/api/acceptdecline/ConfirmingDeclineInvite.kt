/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invite.api.acceptdecline

import com.echo.features.invite.api.InviteData
import com.echo.libraries.architecture.AsyncAction

data class ConfirmingDeclineInvite(val inviteData: InviteData, val blockUser: Boolean) : AsyncAction.Confirming

