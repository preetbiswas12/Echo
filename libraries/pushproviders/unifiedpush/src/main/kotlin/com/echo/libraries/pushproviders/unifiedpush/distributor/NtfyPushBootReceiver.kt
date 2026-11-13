/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush.distributor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Restart the push distributor service after a reboot or an application update, so the
 * WebSocket subscriptions to the registered topics are restored.
 */
class NtfyPushBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> NtfyPushService.start(context)
        }
    }
}
