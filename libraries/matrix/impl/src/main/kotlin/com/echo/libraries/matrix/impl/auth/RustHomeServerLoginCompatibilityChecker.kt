/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.auth

import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.core.extensions.runCatchingExceptions
import com.echo.libraries.matrix.api.auth.HomeServerLoginCompatibilityChecker
import com.echo.libraries.matrix.impl.ClientBuilderProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext
import timber.log.Timber

@ContributesBinding(AppScope::class)
class RustHomeServerLoginCompatibilityChecker(
    private val clientBuilderProvider: ClientBuilderProvider,
    private val coroutineDispatchers: CoroutineDispatchers,
) : HomeServerLoginCompatibilityChecker {
    override suspend fun check(url: String): Result<Boolean> = withContext(coroutineDispatchers.io) {
        runCatchingExceptions {
            clientBuilderProvider.provide()
                .inMemoryStore()
                .serverNameOrHomeserverUrl(url)
                .build()
                .use {
                    it.homeserverLoginDetails()
                }
                .use {
                    Timber.d("Homeserver $url | OAuth: ${it.supportsOauthLogin()} | Password: ${it.supportsPasswordLogin()} | SSO: ${it.supportsSsoLogin()}")
                    it.supportsOauthLogin() || it.supportsPasswordLogin()
                }
        }
    }
}
