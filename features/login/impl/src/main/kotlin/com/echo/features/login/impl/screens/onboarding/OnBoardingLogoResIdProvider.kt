/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.onboarding

import android.annotation.SuppressLint
import android.content.Context
import com.echo.libraries.di.annotations.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

fun interface OnBoardingLogoResIdProvider {
    fun get(): Int?
}

@ContributesBinding(AppScope::class)
class DefaultOnBoardingLogoResIdProvider(
    @ApplicationContext private val context: Context,
) : OnBoardingLogoResIdProvider {
    @SuppressLint("DiscouragedApi")
    override fun get(): Int? {
        val resId = context.resources
            .getIdentifier("onboarding_logo", "drawable", context.packageName)
            .takeIf { it != 0 }
        return resId
    }
}
