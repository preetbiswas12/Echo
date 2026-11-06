/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.x

import android.app.Application
import android.os.StrictMode
import androidx.compose.material3.ComposeMaterial3Flags.isAnchoredDraggableComponentsStrictOffsetCheckEnabled
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.startup.AppInitializer
import androidx.work.Configuration
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import io.element.android.call.ui.ElementCallActivity
import com.echo.libraries.architecture.bindings
import com.echo.libraries.di.DependencyInjectionGraphOwner
import com.echo.libraries.matrix.api.SdkMetadata
import com.echo.libraries.workmanager.api.di.MetroWorkerFactory
import com.echo.x.call.ElementCallActivityHostDelegate
import com.echo.x.di.AppBindings
import com.echo.x.di.AppGraph
import com.echo.x.di.ApplicationBindings
import com.echo.x.info.logApplicationInfo
import com.echo.x.initializer.CacheCleanerInitializer
import com.echo.x.initializer.CrashInitializer
import com.echo.x.initializer.PlatformInitializer

// The private constructor is there so the native call screen's host can be a delegate: Android instantiates
// the Application through the public no-arg one.
class ElementXApplication private constructor(
    private val elementCallActivityHost: ElementCallActivityHostDelegate,
) : Application(),
    DependencyInjectionGraphOwner,
    Configuration.Provider,
    ElementCallActivity.Host by elementCallActivityHost {
    constructor() : this(ElementCallActivityHostDelegate())

    override val graph: AppGraph = createGraphFactory<AppGraph.Factory>().create(this)

    override val workManagerConfiguration: Configuration = Configuration.Builder()
        .setWorkerFactory(MetroWorkerFactory(graph.workerProviders))
        .build()

    @Inject lateinit var sdkMetadata: SdkMetadata

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate() {
        super.onCreate()

        enableStrictMode()

        AppInitializer.getInstance(this).apply {
            initializeComponent(CrashInitializer::class.java)
            initializeComponent(PlatformInitializer::class.java)
            initializeComponent(CacheCleanerInitializer::class.java)
        }

        bindings<ApplicationBindings>().inject(this)
        elementCallActivityHost.bind { bindings<AppBindings>().nativeCallScreenHost() }
        logApplicationInfo(this, sdkMetadata.sdkGitSha)

        // Disable the strict offset check for anchored draggable components, as it can cause issues with bottom sheets.
        // Remove once https://issuetracker.google.com/issues/477038695 is fixed.
        isAnchoredDraggableComponentsStrictOffsetCheckEnabled = false
    }

    private fun enableStrictMode() {
        // Workaround to avoid NetworkOnMainThreadException when using the network on the main thread.
        // This is a temporary solution to rustls doing the certificate verification using blocking network requests.
        // It should be removed once the underlying issue is fixed.
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
    }
}

