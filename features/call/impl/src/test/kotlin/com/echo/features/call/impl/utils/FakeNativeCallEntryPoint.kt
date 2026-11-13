/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.call.impl.utils

import com.echo.features.call.api.CallData
import com.echo.features.callnative.api.NativeCallEntryPoint

class FakeNativeCallEntryPoint(
    private val startCallLambda: (CallData) -> Unit = {},
) : NativeCallEntryPoint {
    override fun startCall(callData: CallData) = startCallLambda(callData)
}
