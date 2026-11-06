/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.lockscreen.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import com.echo.features.lockscreen.impl.LockScreenConfig

@ContributesTo(AppScope::class)
@BindingContainer
object LockScreenConfigBindingContainer {
    @Provides
    fun providesLockScreenConfig(): LockScreenConfig = LockScreenConfig(
        isPinMandatory = com.echo.appconfig.LockScreenConfig.IS_PIN_MANDATORY,
        forbiddenPinCodes = com.echo.appconfig.LockScreenConfig.FORBIDDEN_PIN_CODES,
        pinSize = com.echo.appconfig.LockScreenConfig.PIN_SIZE,
        maxPinCodeAttemptsBeforeLogout = com.echo.appconfig.LockScreenConfig.MAX_PIN_CODE_ATTEMPTS_BEFORE_LOGOUT,
        gracePeriod = com.echo.appconfig.LockScreenConfig.GRACE_PERIOD,
        isStrongBiometricsEnabled = com.echo.appconfig.LockScreenConfig.IS_STRONG_BIOMETRICS_ENABLED,
        isWeakBiometricsEnabled = com.echo.appconfig.LockScreenConfig.IS_WEAK_BIOMETRICS_ENABLED,
    )
}

