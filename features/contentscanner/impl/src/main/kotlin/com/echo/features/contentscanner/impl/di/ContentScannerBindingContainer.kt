/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.contentscanner.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import com.echo.features.contentscanner.api.ContentScannerService
import com.echo.features.contentscanner.impl.DefaultContentScannerService
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.di.RoomScope
import com.echo.libraries.di.annotations.RoomCoroutineScope
import com.echo.libraries.matrix.api.scanner.ContentScanner
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(RoomScope::class)
object ContentScannerBindingContainer {
    @Provides
    @SingleIn(RoomScope::class)
    fun providesContentScannerService(
        contentScanner: ContentScanner?,
        @RoomCoroutineScope coroutineScope: CoroutineScope,
        coroutineDispatchers: CoroutineDispatchers,
    ): ContentScannerService {
        return if (contentScanner != null) {
            DefaultContentScannerService(
                contentScanner = contentScanner,
                coroutineScope = coroutineScope,
                coroutineDispatchers = coroutineDispatchers,
            )
        } else {
            AlwaysValidContentScannerService()
        }
    }
}

