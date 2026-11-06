/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.login

import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog

fun aLoginModeState(
    loginMode: AsyncData<LoginMode> = AsyncData.Uninitialized,
    localNetworkPermissionDialog: LocalNetworkPermissionDialog = LocalNetworkPermissionDialog.None,
    eventSink: (LoginModeEvent) -> Unit = {},
) = LoginModeState(
    loginMode = loginMode,
    localNetworkPermissionDialog = localNetworkPermissionDialog,
    eventSink = eventSink,
)

