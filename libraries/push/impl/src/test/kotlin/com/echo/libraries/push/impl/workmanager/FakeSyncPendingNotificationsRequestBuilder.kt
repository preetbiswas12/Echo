/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.workmanager

import com.echo.libraries.workmanager.api.WorkManagerRequestWrapper

class FakeSyncPendingNotificationsRequestBuilder(
    private val build: () -> Result<List<WorkManagerRequestWrapper>> = { Result.success(emptyList()) },
) : SyncPendingNotificationsRequestBuilder {
    override suspend fun build(): Result<List<WorkManagerRequestWrapper>> = build.invoke()
}

