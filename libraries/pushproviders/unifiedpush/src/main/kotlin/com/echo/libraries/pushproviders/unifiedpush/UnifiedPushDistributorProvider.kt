/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush

import android.content.Context
import com.echo.libraries.androidutils.system.getApplicationLabel
import com.echo.libraries.di.annotations.ApplicationContext
import com.echo.libraries.pushproviders.api.Distributor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import org.unifiedpush.android.connector.UnifiedPush

interface UnifiedPushDistributorProvider {
    fun getDistributors(): List<Distributor>
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushDistributorProvider(
    @ApplicationContext private val context: Context,
) : UnifiedPushDistributorProvider {
    override fun getDistributors(): List<Distributor> {
        // Echo embeds its own UnifiedPush distributor (see the `distributor` package), so this
        // package is a valid distributor and is included. Upstream excluded it because it was
        // only a placeholder used to keep the background sync as an internal distributor.
        return UnifiedPush.getDistributors(context).map {
            Distributor(it, context.getApplicationLabel(it))
        }
    }
}
