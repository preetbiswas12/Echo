/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.x.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import com.echo.features.callnative.api.NativeCallScreenHost
import com.echo.features.enterprise.api.AppStartupHook
import com.echo.features.enterprise.api.EnterpriseService
import com.echo.features.lockscreen.api.LockScreenEntryPoint
import com.echo.features.lockscreen.api.LockScreenService
import com.echo.features.migration.api.MigrationEntryPoint
import com.echo.features.rageshake.api.reporter.BugReporter
import com.echo.libraries.core.meta.BuildMeta
import com.echo.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import com.echo.libraries.di.identifiers.SentrySdkDsn
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.matrix.api.platform.InitPlatformService
import com.echo.libraries.matrix.api.tracing.TracingService
import com.echo.libraries.preferences.api.store.AppPreferencesStore
import com.echo.services.analytics.api.AnalyticsService

@ContributesTo(AppScope::class)
interface AppBindings {
    fun snackbarDispatcher(): SnackbarDispatcher

    fun tracingService(): TracingService

    fun platformService(): InitPlatformService

    fun bugReporter(): BugReporter

    fun lockScreenService(): LockScreenService

    fun preferencesStore(): AppPreferencesStore

    fun migrationEntryPoint(): MigrationEntryPoint

    fun lockScreenEntryPoint(): LockScreenEntryPoint

    fun analyticsService(): AnalyticsService

    fun enterpriseService(): EnterpriseService

    fun featureFlagService(): FeatureFlagService

    fun buildMeta(): BuildMeta

    /** For `ElementXApplication`, which the native call screen asks for its host. */
    fun nativeCallScreenHost(): NativeCallScreenHost

    fun sentrySdkDsn(): SentrySdkDsn?

    @Multibinds(allowEmpty = true)
    fun appStartupHooks(): Set<AppStartupHook>
}

