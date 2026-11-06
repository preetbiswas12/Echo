/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("SameParameterValue")

package com.echo.x.intent

import android.content.Context
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.echo.libraries.deeplink.api.DeepLinkCreator
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.matrix.test.AN_EVENT_ID
import com.echo.libraries.matrix.test.A_ROOM_ID
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.matrix.test.A_THREAD_ID
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import com.echo.tests.testutils.robolectric.RobolectricTest
import com.echo.x.MainActivity
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class DefaultIntentProviderTest : RobolectricTest() {
    @Test
    fun `test getViewRoomIntent with data`() {
        val deepLinkCreator = lambdaRecorder<SessionId, RoomId?, ThreadId?, EventId?, String> { _, _, _, _ -> "deepLinkCreatorResult" }
        val sut = createDefaultIntentProvider(
            deepLinkCreator = { sessionId, roomId, threadId, eventId -> deepLinkCreator.invoke(sessionId, roomId, threadId, eventId) },
        )
        val result = sut.getViewRoomIntent(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            threadId = A_THREAD_ID,
            eventId = AN_EVENT_ID,
        )
        result.commonAssertions()
        assertThat(result.data.toString()).isEqualTo("deepLinkCreatorResult")
        deepLinkCreator.assertions().isCalledOnce().with(
            value(A_SESSION_ID),
            value(A_ROOM_ID),
            value(A_THREAD_ID),
            value(AN_EVENT_ID),
        )
    }

    private fun createDefaultIntentProvider(
        deepLinkCreator: DeepLinkCreator = DeepLinkCreator { _, _, _, _ -> "" },
    ): DefaultIntentProvider {
        return DefaultIntentProvider(
            context = RuntimeEnvironment.getApplication() as Context,
            deepLinkCreator = deepLinkCreator,
        )
    }

    private fun Intent.commonAssertions() {
        assertThat(action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(component?.className).isEqualTo(MainActivity::class.java.name)
    }
}

