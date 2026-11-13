/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.pushgateway

import com.echo.libraries.network.RetrofitFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

interface PushGatewayApiFactory {
    fun create(baseUrl: String): PushGatewayAPI
}

@ContributesBinding(AppScope::class)
class DefaultPushGatewayApiFactory(
    private val retrofitFactory: RetrofitFactory,
) : PushGatewayApiFactory {
    override fun create(baseUrl: String): PushGatewayAPI {
        return retrofitFactory.create(baseUrl)
            .create(PushGatewayAPI::class.java)
    }
}
