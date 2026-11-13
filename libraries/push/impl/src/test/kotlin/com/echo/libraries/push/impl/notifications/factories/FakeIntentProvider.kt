/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.notifications.factories

import android.content.Intent
import android.os.Bundle
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.push.impl.intent.IntentProvider

class FakeIntentProvider : IntentProvider {
    override fun getViewRoomIntent(
        sessionId: SessionId,
        roomId: RoomId?,
        threadId: ThreadId?,
        eventId: EventId?,
        extras: Bundle?,
    ) = Intent(Intent.ACTION_VIEW)
}
