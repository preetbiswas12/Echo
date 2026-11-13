/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.services.analytics.noop.watchers

import com.echo.libraries.di.RoomScope
import com.echo.services.analytics.api.watchers.AnalyticsSendMessageWatcher
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(RoomScope::class)
class NoopAnalyticsSendMessageWatcher : AnalyticsSendMessageWatcher {
    override fun start() = Unit
    override fun stop() = Unit
}
