/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.cachestore.impl

import com.echo.libraries.cachestore.api.CacheData
import com.echo.libraries.cachestore.api.CacheStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DatabaseCacheStore(
    private val database: CacheDatabase,
) : CacheStore {
    override suspend fun getData(key: String): CacheData? {
        return database.cacheDataQueries.selectData(key)
            .executeAsOneOrNull()
            ?.toApiModel()
    }

    override suspend fun storeData(key: String, data: CacheData) {
        database.cacheDataQueries.insertData(
            data.toDbModel(key)
        ).await()
    }

    override suspend fun deleteData(key: String) {
        database.cacheDataQueries.deleteData(key).await()
    }

    override suspend fun deleteAll() {
        database.cacheDataQueries.deleteAll().await()
    }
}
