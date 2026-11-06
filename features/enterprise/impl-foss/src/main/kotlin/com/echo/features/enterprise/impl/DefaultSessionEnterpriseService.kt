/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.enterprise.impl

import dev.zacsweers.metro.ContributesBinding
import com.echo.features.enterprise.api.SessionEnterpriseService
import com.echo.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultSessionEnterpriseService : SessionEnterpriseService {
    override suspend fun init() = Unit
    override suspend fun tweakMasUrl(url: String): String = url
    override suspend fun isElementCallAvailable(): Boolean = true
    override suspend fun isEncryptionDisabledByHomeserver(): Boolean = false
    override suspend fun arePublicRoomsDisabledByHomeserver(): Boolean = false
}

