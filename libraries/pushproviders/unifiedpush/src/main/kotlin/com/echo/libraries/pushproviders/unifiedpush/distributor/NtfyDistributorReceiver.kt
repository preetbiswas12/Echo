/*
 * Copyright (c) 2026 Echo contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush.distributor

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.echo.libraries.pushproviders.unifiedpush.distributor.UnifiedPushSpec.ACTION_NEW_ENDPOINT
import com.echo.libraries.pushproviders.unifiedpush.distributor.UnifiedPushSpec.ACTION_UNREGISTERED
import com.echo.libraries.pushproviders.unifiedpush.distributor.UnifiedPushSpec.EXTRA_ENDPOINT
import com.echo.libraries.pushproviders.unifiedpush.distributor.UnifiedPushSpec.EXTRA_PENDING_INTENT
import com.echo.libraries.pushproviders.unifiedpush.distributor.UnifiedPushSpec.EXTRA_TOKEN
import timber.log.Timber

private const val LOG_TAG = "NtfyDistributorReceiver"

/**
 * Registration receiver of the UnifiedPush distributor embedded in Echo, as defined by
 * https://unifiedpush.org/developers/spec/android/.
 *
 * On [UnifiedPushSpec.ACTION_REGISTER], it maps the connection token to a new ntfy topic,
 * makes sure [NtfyPushService] subscribes to it, and answers with a `NEW_ENDPOINT` broadcast
 * pointing to the topic on the baked-in ntfy server. Echo itself is the only client, but the
 * protocol is implemented generically so any UnifiedPush application on the device can use it.
 */
class NtfyDistributorReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            UnifiedPushSpec.ACTION_REGISTER -> onRegister(context, intent)
            UnifiedPushSpec.ACTION_UNREGISTER -> onUnregister(context, intent)
            // The service delivers each message only once, there is nothing to acknowledge.
            UnifiedPushSpec.ACTION_MESSAGE_ACK -> Unit
        }
    }

    private fun onRegister(context: Context, intent: Intent) {
        val senderPackage = intent.getSenderPackage(this)
        val token = intent.getStringExtra(EXTRA_TOKEN)
        if (senderPackage == null || token == null) {
            Timber.tag(LOG_TAG).w("Ignoring registration with missing sender package or token")
            return
        }
        val registration = NtfyDistributorStore(context).register(senderPackage, token)
        Timber.tag(LOG_TAG).i("Registered a new push application")

        // Make sure the service subscribes to the topic of the registration.
        NtfyPushService.requestSync(context)

        context.sendBroadcast(
            Intent(ACTION_NEW_ENDPOINT).apply {
                `package` = senderPackage
                putExtra(EXTRA_TOKEN, token)
                putExtra(EXTRA_ENDPOINT, "${DistributorConfig.NTFY_SERVER_BASE_URL}/${registration.topic}")
            }
        )
    }

    private fun onUnregister(context: Context, intent: Intent) {
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: return
        val removed = NtfyDistributorStore(context).remove(token) ?: return
        Timber.tag(LOG_TAG).i("Unregistered a push application")

        context.sendBroadcast(
            Intent(ACTION_UNREGISTERED).apply {
                `package` = removed.packageName
                putExtra(EXTRA_TOKEN, token)
            }
        )

        // Stop the service if there is no registration anymore.
        NtfyPushService.requestSync(context)
    }

    /**
     * The package name of the application which sent the broadcast:
     * - from SDK 34, broadcasts are sent with the shared identity option, and the package
     *   name of the sender is available on the receiver,
     * - before that, the sender includes a `PendingIntent` to a dummy app whose creator is
     *   the sender application.
     */
    private fun Intent.getSenderPackage(receiver: BroadcastReceiver): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            receiver.getSentFromPackage()
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra<PendingIntent>(EXTRA_PENDING_INTENT)?.creatorPackage
        }
    }
}
