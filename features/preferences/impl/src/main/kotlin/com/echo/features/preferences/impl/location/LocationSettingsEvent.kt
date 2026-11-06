/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.location

sealed interface LocationSettingsEvent {
    data class SetLiveLocationMinimumDistanceUpdate(val value: Int) : LocationSettingsEvent
}

