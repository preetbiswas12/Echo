/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.analytics.impl

import com.echo.features.analytics.api.AnalyticsOptInEvent

data class AnalyticsOptInState(
    val applicationName: String,
    val hasPolicyLink: Boolean,
    val eventSink: (AnalyticsOptInEvent) -> Unit
)

