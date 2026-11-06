/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.test

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.push.impl.BuildConfig
import com.echo.libraries.push.impl.pushgateway.PushGatewayNotifyRequest
import com.echo.libraries.pushproviders.api.Config

interface TestPush {
    suspend fun execute(config: Config)
}

@ContributesBinding(AppScope::class)
class DefaultTestPush(
    private val pushGatewayNotifyRequest: PushGatewayNotifyRequest,
) : TestPush {
    override suspend fun execute(config: Config) {
        pushGatewayNotifyRequest.execute(
            PushGatewayNotifyRequest.Params(
                url = config.url,
                appId = BuildConfig.PUSHER_APP_ID,
                pushKey = config.pushKey,
                eventId = TEST_EVENT_ID,
                roomId = TEST_ROOM_ID,
            )
        )
    }

    companion object {
        val TEST_EVENT_ID = EventId("\$THIS_IS_A_FAKE_EVENT_ID")
        val TEST_ROOM_ID = RoomId("!room:domain")
    }
}

