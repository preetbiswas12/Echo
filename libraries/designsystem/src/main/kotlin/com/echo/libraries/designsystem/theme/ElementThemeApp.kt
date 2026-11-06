/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.designsystem.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.echo.compound.theme.ElementTheme
import com.echo.compound.theme.Theme
import com.echo.compound.theme.mapToTheme
import com.echo.compound.tokens.generated.SemanticColors
import com.echo.libraries.core.meta.BuildMeta
import com.echo.libraries.core.meta.BuildType
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.preferences.api.store.AppPreferencesStore

val LocalBuildMeta = staticCompositionLocalOf {
    BuildMeta(
        isDebuggable = true,
        buildType = BuildType.DEBUG,
        applicationName = "MyApp",
        productionApplicationName = "MyAppProd",
        desktopApplicationName = "MyAppDesktop",
        applicationId = "AppId",
        isEnterpriseBuild = false,
        lowPrivacyLoggingEnabled = false,
        versionName = "aVersion",
        versionCode = 123,
        gitRevision = "aRevision",
        gitBranchName = "aBranch",
        flavorDescription = "aFlavor",
        flavorShortDescription = "aFlavorShort",
    )
}

/**
 * Theme to use for all the regular screens of the application.
 * Will manage the light / dark theme based on the user preference.
 * Will also ensure that the system is applying the correct global theme
 * to the application, especially when the system is light and the application
 * is forced to use dark theme.
 */
@Composable
fun ElementThemeApp(
    appPreferencesStore: AppPreferencesStore,
    featureFlagService: FeatureFlagService,
    compoundLight: SemanticColors,
    compoundDark: SemanticColors,
    buildMeta: BuildMeta,
    content: @Composable () -> Unit,
) {
    val isBlackThemeAllowed by remember {
        featureFlagService.isFeatureEnabledFlow(FeatureFlags.AllowBlackTheme)
    }.collectAsState(initial = false)
    val theme by remember(isBlackThemeAllowed) {
        appPreferencesStore.getThemeFlow().mapToTheme(allowBlackTheme = isBlackThemeAllowed)
    }.collectAsState(initial = Theme.System)
    LaunchedEffect(theme) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                Theme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                Theme.Light -> AppCompatDelegate.MODE_NIGHT_NO
                Theme.Dark, Theme.Black -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }
    CompositionLocalProvider(
        LocalBuildMeta provides buildMeta,
    ) {
        ElementTheme(
            theme = theme,
            content = content,
            compoundLight = compoundLight,
            compoundDark = compoundDark,
        )
    }
}

