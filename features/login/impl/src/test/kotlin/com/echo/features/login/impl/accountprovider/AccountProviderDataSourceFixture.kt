/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.accountprovider

import com.echo.features.enterprise.api.EnterpriseService
import com.echo.features.enterprise.test.FakeEnterpriseService
import com.echo.libraries.preferences.api.store.AppPreferencesStore
import com.echo.libraries.preferences.test.InMemoryAppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun anAccountProviderDataSource(
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
    appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) = AccountProviderDataSource(
    enterpriseService = enterpriseService,
    appPreferencesStore = appPreferencesStore,
    coroutineScope = coroutineScope,
)

