/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.troubleshoot

import androidx.compose.ui.graphics.toArgb
import com.echo.appconfig.NotificationConfig
import com.echo.features.enterprise.api.EnterpriseService
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.push.impl.R
import com.echo.libraries.push.impl.notifications.NotificationDisplayer
import com.echo.libraries.push.impl.notifications.factories.NotificationCreator
import com.echo.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import com.echo.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import com.echo.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import com.echo.services.toolbox.api.strings.StringProvider
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@ContributesIntoSet(SessionScope::class)
class NotificationTest(
    private val sessionId: SessionId,
    private val notificationCreator: NotificationCreator,
    private val notificationDisplayer: NotificationDisplayer,
    private val notificationClickHandler: NotificationClickHandler,
    private val stringProvider: StringProvider,
    private val enterpriseService: EnterpriseService,
) : NotificationTroubleshootTest {
    override val order = 50
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_description),
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val color = enterpriseService.brandColorsFlow(sessionId).first()?.toArgb()
            ?: NotificationConfig.NOTIFICATION_ACCENT_COLOR
        val notification = notificationCreator.createDiagnosticNotification(color)
        val result = notificationDisplayer.displayDiagnosticNotification(notification)
        if (result) {
            coroutineScope.listenToNotificationClick()
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_waiting),
                status = NotificationTroubleshootTestState.Status.WaitingForUser
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_permission_failure),
                status = NotificationTroubleshootTestState.Status.Failure()
            )
        }
    }

    private fun CoroutineScope.listenToNotificationClick() = launch {
        val job = launch {
            notificationClickHandler.state.first()
            Timber.d("Notification clicked!")
        }
        @Suppress("RunCatchingNotAllowed")
        runCatching {
            withTimeout(30.seconds) {
                job.join()
            }
        }.fold(
            onSuccess = {
                delegate.updateState(
                    description = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_success),
                    status = NotificationTroubleshootTestState.Status.Success
                )
            },
            onFailure = {
                job.cancel()
                notificationDisplayer.dismissDiagnosticNotification()
                delegate.updateState(
                    description = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_failure),
                    status = NotificationTroubleshootTestState.Status.Failure()
                )
            }
        )
    }.invokeOnCompletion {
        // Ensure that the notification is cancelled when the screen is left
        notificationDisplayer.dismissDiagnosticNotification()
    }

    override suspend fun reset() = delegate.reset()
}
