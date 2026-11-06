/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.androidutils.toast

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.androidutils.system.toast
import com.echo.libraries.di.annotations.ApplicationContext

@ContributesBinding(AppScope::class)
class AndroidToastHelper(
    @ApplicationContext private val context: Context,
) : ToastHelper {
    override fun show(resId: Int) {
        context.toast(resId)
    }
}

