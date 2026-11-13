/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.deeplink.impl

import com.echo.libraries.androidutils.text.urlEncoded
import com.echo.libraries.deeplink.api.DeepLinkCreator
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.ThreadId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class DefaultDeepLinkCreator : DeepLinkCreator {
    override fun create(sessionId: SessionId, roomId: RoomId?, threadId: ThreadId?, eventId: EventId?): String {
        return buildString {
            append("$SCHEME://$HOST/")
            append(sessionId.value.urlEncoded())
            append("/")
            append(roomId?.value?.urlEncoded().orEmpty())
            append("/")
            append(threadId?.value?.urlEncoded().orEmpty())
            append("/")
            append(eventId?.value?.urlEncoded().orEmpty())
        }
            // Remove all possible trailing '/' characters:
            // No event id
            .removeSuffix("/")
            // No thread id
            .removeSuffix("/")
            // No room id
            .removeSuffix("/")
    }
}
