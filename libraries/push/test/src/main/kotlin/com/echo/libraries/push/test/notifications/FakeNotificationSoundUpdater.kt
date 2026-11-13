/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.test.notifications

import com.echo.libraries.preferences.api.store.NotificationSound
import com.echo.libraries.push.api.notifications.NotificationSoundUpdater
import com.echo.tests.testutils.lambda.lambdaError

class FakeNotificationSoundUpdater(
    private val recreateNoisyChannelLambda: (sound: NotificationSound, version: Int) -> Unit = { _, _ -> lambdaError() },
    private val recreateRingingCallChannelLambda: (sound: NotificationSound, version: Int) -> Unit = { _, _ -> lambdaError() },
    private val readNoisyChannelSoundLambda: () -> NotificationSound? = { null },
    private val readRingingCallChannelSoundLambda: () -> NotificationSound? = { null },
) : NotificationSoundUpdater {
    override fun recreateNoisyChannel(sound: NotificationSound, version: Int) {
        recreateNoisyChannelLambda(sound, version)
    }

    override fun recreateRingingCallChannel(sound: NotificationSound, version: Int) {
        recreateRingingCallChannelLambda(sound, version)
    }

    override suspend fun readNoisyChannelSound(): NotificationSound? = readNoisyChannelSoundLambda()

    override suspend fun readRingingCallChannelSound(): NotificationSound? = readRingingCallChannelSoundLambda()
}
