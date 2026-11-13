/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.developer.appsettings

import com.echo.features.preferences.impl.developer.tracing.LogLevelItem
import com.echo.libraries.featureflag.ui.model.FeatureUiModel
import com.echo.libraries.matrix.api.tracing.TraceLogPack

sealed interface AppDeveloperSettingsEvent {
    data class SetDeveloperModeEnabled(val enabled: Boolean) : AppDeveloperSettingsEvent
    data class UpdateEnabledFeature(val feature: FeatureUiModel, val isEnabled: Boolean) : AppDeveloperSettingsEvent
    data class SetCustomElementCallBaseUrl(val baseUrl: String?) : AppDeveloperSettingsEvent
    data class SetTracingLogLevel(val logLevel: LogLevelItem) : AppDeveloperSettingsEvent
    data class ToggleTracingLogPack(val logPack: TraceLogPack, val enabled: Boolean) : AppDeveloperSettingsEvent
    data class CopyToClipboard(val text: String) : AppDeveloperSettingsEvent
}
