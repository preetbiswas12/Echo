/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.contentscanner.impl.di

import com.echo.features.contentscanner.impl.DefaultContentScannerService
import com.echo.libraries.matrix.api.scanner.ContentScanner
import com.echo.libraries.matrix.test.scanner.FakeContentScanner
import com.echo.tests.testutils.testCoroutineDispatchers
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ContentScannerBindingContainerTest {
    @Test
    fun `ContentScannerBindingContainer provides NoopContentScannerService when ContentScanner is null`() = runTest {
        // Given
        val contentScanner: ContentScanner? = null
        val sessionCoroutineScope = backgroundScope
        val coroutineDispatchers = testCoroutineDispatchers()

        // When
        val contentScannerService = ContentScannerBindingContainer.providesContentScannerService(
            contentScanner = contentScanner,
            coroutineScope = sessionCoroutineScope,
            coroutineDispatchers = coroutineDispatchers,
        )

        // Then
        assertThat(contentScannerService).isInstanceOf(AlwaysValidContentScannerService::class.java)
    }

    @Test
    fun `ContentScannerBindingContainer provides DefaultContentScannerService when ContentScanner is not null`() = runTest {
        // Given
        val contentScanner: ContentScanner? = FakeContentScanner()
        val sessionCoroutineScope = backgroundScope
        val coroutineDispatchers = testCoroutineDispatchers()

        // When
        val contentScannerService = ContentScannerBindingContainer.providesContentScannerService(
            contentScanner = contentScanner,
            coroutineScope = sessionCoroutineScope,
            coroutineDispatchers = coroutineDispatchers,
        )

        // Then
        assertThat(contentScannerService).isInstanceOf(DefaultContentScannerService::class.java)
    }
}
