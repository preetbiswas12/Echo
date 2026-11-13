/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush

import com.echo.features.enterprise.api.EnterpriseService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

interface DefaultPushGatewayHttpUrlProvider {
    fun provide(): String
}

@ContributesBinding(AppScope::class)
class DefaultDefaultPushGatewayHttpUrlProvider(
    private val enterpriseService: EnterpriseService,
) : DefaultPushGatewayHttpUrlProvider {
    override fun provide(): String {
        return enterpriseService.unifiedPushDefaultPushGateway() ?: UnifiedPushConfig.DEFAULT_PUSH_GATEWAY_HTTP_URL
    }
}
