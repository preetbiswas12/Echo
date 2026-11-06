/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.lockscreen.test

import com.echo.features.lockscreen.api.DeviceUnlockEntryPoint
import com.echo.tests.testutils.EnsureNeverCalled

class FakeDeviceUnlockEntryPointCallback(
    private val onCancelLambda: () -> Unit = EnsureNeverCalled(),
    private val onUnlockedLambda: () -> Unit = EnsureNeverCalled(),
) : DeviceUnlockEntryPoint.Callback {
    override fun onCancel() = onCancelLambda()
    override fun onUnlock() = onUnlockedLambda()
}

