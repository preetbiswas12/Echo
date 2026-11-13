/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.appnav.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.echo.features.rageshake.api.crash.CrashDetectionState
import com.echo.features.rageshake.api.detection.RageshakeDetectionState
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.matrix.api.SdkMetadata
import com.echo.services.analytics.api.AnalyticsService
import com.echo.services.apperror.api.AppErrorStateService
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.plan.SuperProperties

@Inject
class RootPresenter(
    private val crashDetectionPresenter: Presenter<CrashDetectionState>,
    private val rageshakeDetectionPresenter: Presenter<RageshakeDetectionState>,
    private val appErrorStateService: AppErrorStateService,
    private val analyticsService: AnalyticsService,
    private val sdkMetadata: SdkMetadata,
) : Presenter<RootState> {
    @Composable
    override fun present(): RootState {
        val rageshakeDetectionState = rageshakeDetectionPresenter.present()
        val crashDetectionState = crashDetectionPresenter.present()
        val appErrorState by appErrorStateService.appErrorStateFlow.collectAsState()

        LaunchedEffect(Unit) {
            analyticsService.updateSuperProperties(
                SuperProperties(
                    cryptoSDK = SuperProperties.CryptoSDK.Rust,
                    appPlatform = SuperProperties.AppPlatform.EXA,
                    cryptoSDKVersion = sdkMetadata.sdkGitSha,
                )
            )
        }

        return RootState(
            rageshakeDetectionState = rageshakeDetectionState,
            crashDetectionState = crashDetectionState,
            errorState = appErrorState,
        )
    }
}
