/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.x.intent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import com.echo.libraries.deeplink.api.DeepLinkCreator
import com.echo.libraries.di.annotations.ApplicationContext
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.push.impl.intent.IntentProvider
import com.echo.x.MainActivity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class DefaultIntentProvider(
    @ApplicationContext private val context: Context,
    private val deepLinkCreator: DeepLinkCreator,
) : IntentProvider {
    override fun getViewRoomIntent(
        sessionId: SessionId,
        roomId: RoomId?,
        threadId: ThreadId?,
        eventId: EventId?,
        extras: Bundle?,
    ): Intent {
        return Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = deepLinkCreator.create(sessionId, roomId, threadId, eventId).toUri()
            extras?.let(::putExtras)
        }
    }
}
