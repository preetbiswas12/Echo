/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.services.toolbox.impl.intent

import android.content.Context
import android.content.Intent
import com.echo.libraries.di.annotations.ApplicationContext
import com.echo.services.toolbox.api.intent.ExternalIntentLauncher
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class DefaultExternalIntentLauncher(
    @ApplicationContext private val context: Context,
) : ExternalIntentLauncher {
    override fun launch(intent: Intent) {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
