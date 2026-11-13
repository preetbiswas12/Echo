/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.services.analytics.noop.watchers

import com.echo.libraries.di.SessionScope
import com.echo.services.analytics.api.watchers.AnalyticsRoomListStateWatcher
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(SessionScope::class)
class NoopAnalyticsRoomListStateWatcher : AnalyticsRoomListStateWatcher {
    override fun start() {}
    override fun stop() {}
}
