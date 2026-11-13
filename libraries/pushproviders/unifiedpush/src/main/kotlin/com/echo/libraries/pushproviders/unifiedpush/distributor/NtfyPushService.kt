/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush.distributor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.echo.libraries.core.extensions.runCatchingExceptions
import com.echo.libraries.pushproviders.unifiedpush.R
import com.echo.libraries.pushproviders.unifiedpush.distributor.UnifiedPushSpec.ACTION_MESSAGE
import com.echo.libraries.pushproviders.unifiedpush.distributor.UnifiedPushSpec.EXTRA_BYTES_MESSAGE
import com.echo.libraries.pushproviders.unifiedpush.distributor.UnifiedPushSpec.EXTRA_MESSAGE_ID
import com.echo.libraries.pushproviders.unifiedpush.distributor.UnifiedPushSpec.EXTRA_TOKEN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val NOTIFICATION_ID = 1002
private const val CHANNEL_ID = "push_distributor_notification_channel"

// Never log a full topic or endpoint: topics are capability URLs that grant access to the push messages.
private const val LOG_TAG = "NtfyPushService"

/**
 * A message of the ntfy JSON stream, see https://docs.ntfy.sh/subscribe/api/.
 * Only the fields needed to forward `message` events to the registered applications are parsed.
 */
@Serializable
private data class NtfyStreamMessage(
    val id: String? = null,
    val event: String? = null,
    val topic: String? = null,
    val message: String? = null,
)

/**
 * Foreground service of the embedded UnifiedPush distributor.
 *
 * It maintains one WebSocket subscription per registered ntfy topic, and forwards each message
 * received on a topic to the application that registered it, with the UnifiedPush `MESSAGE`
 * broadcast.
 *
 * The service runs in the foreground so:
 * - the WebSocket connections stay alive when the screen is off (natively, or thanks to the
 *   battery optimization exemption the user can grant to the app),
 * - the process has a foreground importance when a message is delivered, which allows the
 *   application to start its own foreground services to process the push.
 */
class NtfyPushService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(30.seconds.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .build()

    private val webSockets = ConcurrentHashMap<String, WebSocket>()
    private val reconnectJobs = ConcurrentHashMap<String, Job>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startInForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            syncSubscriptions()
        }
        // The system will recreate the service with a null intent if it kills it.
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        webSockets.values.forEach { webSocket ->
            runCatchingExceptions { webSocket.close(CLOSE_NO_REGISTRATION, "Service destroyed") }
        }
        webSockets.clear()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    private fun startInForeground() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(getString(R.string.push_distributor_notification_channel_name))
                .setShowBadge(false)
                .setVibrationEnabled(false)
                .setSound(null, null)
                .build()
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_echo_push_notification)
            .setContentTitle(getString(R.string.push_distributor_notification_title))
            .setContentText(getString(R.string.push_distributor_notification_text))
            .setOngoing(true)
            .setVibrate(longArrayOf(0))
            .setSound(null)
            .build()
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        runCatchingExceptions<Unit> {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceType)
        }
            .onFailure { throwable ->
                Timber.tag(LOG_TAG).e(throwable, "Failed to start the push distributor service in foreground")
                stopSelf()
            }
    }

    /**
     * Make the WebSocket subscriptions match the registered topics: subscribe to the new ones,
     * close the removed ones and stop the service when there is no registration anymore.
     */
    private suspend fun syncSubscriptions() {
        val topics = NtfyDistributorStore(applicationContext)
            .registrations()
            .map { it.topic }
            .distinct()
        Timber.tag(LOG_TAG).i("Syncing subscriptions for %d topic(s)", topics.size)
        if (topics.isEmpty()) {
            Timber.tag(LOG_TAG).i("No push registration anymore, stopping the push distributor service")
            stopSelf()
            return
        }
        webSockets.keys.toList()
            .filter { it !in topics }
            .forEach(::closeSubscription)
        topics.filter { it !in webSockets.keys }
            .forEach { topic -> openSubscription(topic, reconnectAttempt = 0) }
    }

    private fun openSubscription(topic: String, reconnectAttempt: Int) {
        val webSocketUrl = DistributorConfig.NTFY_SERVER_BASE_URL
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
            .let { "$it/$topic/ws" }
        val request = Request.Builder()
            .url(webSocketUrl)
            .build()
        Timber.tag(LOG_TAG).i("Subscribing to a new push topic (attempt %d)", reconnectAttempt + 1)
        val listener = SubscriptionListener(topic, reconnectAttempt)
        webSockets[topic] = okHttpClient.newWebSocket(request, listener)
    }

    private fun closeSubscription(topic: String) {
        reconnectJobs.remove(topic)?.cancel()
        webSockets.remove(topic)?.close(CLOSE_NO_REGISTRATION, "Registration removed")
    }

    private fun onMessageReceived(streamMessage: NtfyStreamMessage) {
        if (streamMessage.event != NTFY_EVENT_MESSAGE) return
        val body = streamMessage.message ?: return
        val topic = streamMessage.topic ?: return
        val registration = NtfyDistributorStore(applicationContext).getForTopic(topic) ?: return

        // The process has a foreground importance thanks to this service, so the application
        // will be allowed to start the foreground services it needs to process the push.
        applicationContext.sendBroadcast(
            Intent(ACTION_MESSAGE).apply {
                `package` = registration.packageName
                putExtra(EXTRA_TOKEN, registration.token)
                putExtra(EXTRA_BYTES_MESSAGE, body.toByteArray())
                streamMessage.id?.let { putExtra(EXTRA_MESSAGE_ID, it) }
            }
        )
    }

    private fun scheduleReconnect(topic: String, reconnectAttempt: Int) {
        if (reconnectJobs[topic]?.isActive == true) return
        val delayMillis = min(
            RECONNECT_BASE_DELAY_MS shl min(reconnectAttempt, RECONNECT_MAX_SHIFT),
            RECONNECT_MAX_DELAY_MS,
        )
        Timber.tag(LOG_TAG).i("Scheduling a reconnection in %d ms", delayMillis)
        reconnectJobs[topic] = serviceScope.launch {
            delay(delayMillis)
            val topics = NtfyDistributorStore(applicationContext).registrations().map { it.topic }
            if (topic in topics) {
                openSubscription(topic, reconnectAttempt + 1)
            } else {
                Timber.tag(LOG_TAG).i("Not reconnecting to a removed topic")
            }
        }
    }

    private inner class SubscriptionListener(
        private val topic: String,
        private val reconnectAttempt: Int,
    ) : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.tag(LOG_TAG).i("Subscribed to a push topic")
            reconnectJobs.remove(topic)?.cancel()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val streamMessage = runCatching { json.decodeFromString<NtfyStreamMessage>(text) }.getOrNull()
            if (streamMessage != null) {
                onMessageReceived(streamMessage)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.tag(LOG_TAG).e(t, "Push subscription failed, will retry")
            webSockets.remove(topic)
            scheduleReconnect(topic, reconnectAttempt)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            webSockets.remove(topic)
        }
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private const val NTFY_EVENT_MESSAGE = "message"
        private const val CLOSE_NO_REGISTRATION = 1000
        private const val RECONNECT_BASE_DELAY_MS = 1_000L
        private const val RECONNECT_MAX_SHIFT = 8
        private val RECONNECT_MAX_DELAY_MS = 5.minutes.inWholeMilliseconds

        /**
         * Make the service sync its WebSocket subscriptions with the current registrations.
         * The service is started if needed, and stopped if there is no registration anymore.
         */
        fun requestSync(context: Context) {
            runCatchingExceptions<Unit> {
                ContextCompat.startForegroundService(context, Intent(context, NtfyPushService::class.java))
            }.onFailure { throwable ->
                Timber.tag(LOG_TAG).e(throwable, "Failed to start the push distributor service")
            }
        }

        /**
         * Start the service only if there is at least one registration, e.g. after a boot.
         */
        fun start(context: Context) {
            if (NtfyDistributorStore(context).registrations().isNotEmpty()) {
                requestSync(context)
            }
        }
    }
}
