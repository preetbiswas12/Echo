/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl.store

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.dateformatter.api.DateFormatter
import com.echo.libraries.dateformatter.api.DateFormatterMode
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.preferences.api.store.PreferenceDataStoreFactory
import com.echo.libraries.push.api.history.PushHistoryItem
import com.echo.libraries.push.impl.PushDatabase
import com.echo.libraries.push.impl.store.DefaultPushDataStore.Companion.BATTERY_OPTIMIZATION_BANNER_STATE_DISMISSED
import com.echo.libraries.push.impl.store.DefaultPushDataStore.Companion.BATTERY_OPTIMIZATION_BANNER_STATE_INIT
import com.echo.libraries.push.impl.store.DefaultPushDataStore.Companion.BATTERY_OPTIMIZATION_BANNER_STATE_SHOW
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
class DefaultPushDataStore(
    private val pushDatabase: PushDatabase,
    private val dateFormatter: DateFormatter,
    private val dispatchers: CoroutineDispatchers,
    preferencesFactory: PreferenceDataStoreFactory,
) : PushDataStore {
    private val pushCounter = intPreferencesKey("push_counter")

    private val dataStore = preferencesFactory.create("push_store")

    /**
     * Integer preference to track the state of the battery optimization banner.
     * Possible values:
     * [BATTERY_OPTIMIZATION_BANNER_STATE_INIT]: Should not show the banner
     * [BATTERY_OPTIMIZATION_BANNER_STATE_SHOW]: Should show the banner
     * [BATTERY_OPTIMIZATION_BANNER_STATE_DISMISSED]: Banner has been shown and user has dismissed it
     */
    private val batteryOptimizationBannerState = intPreferencesKey("battery_optimization_banner_state")

    override val pushCounterFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[pushCounter] ?: 0
    }

    @Suppress("UnnecessaryParentheses")
    override val shouldDisplayBatteryOptimizationBannerFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        (preferences[batteryOptimizationBannerState] ?: BATTERY_OPTIMIZATION_BANNER_STATE_INIT) == BATTERY_OPTIMIZATION_BANNER_STATE_SHOW
    }

    suspend fun incrementPushCounter() {
        dataStore.edit { settings ->
            val currentCounterValue = settings[pushCounter] ?: 0
            settings[pushCounter] = currentCounterValue + 1
        }
    }

    suspend fun setBatteryOptimizationBannerState(newState: Int) {
        dataStore.edit { settings ->
            val currentValue = settings[batteryOptimizationBannerState] ?: BATTERY_OPTIMIZATION_BANNER_STATE_INIT
            settings[batteryOptimizationBannerState] = when (currentValue) {
                BATTERY_OPTIMIZATION_BANNER_STATE_INIT,
                BATTERY_OPTIMIZATION_BANNER_STATE_SHOW -> newState
                BATTERY_OPTIMIZATION_BANNER_STATE_DISMISSED -> currentValue
                else -> error("Invalid value for showBatteryOptimizationBanner: $currentValue")
            }
        }
    }

    override fun getPushHistoryItemsFlow(): Flow<List<PushHistoryItem>> {
        return pushDatabase.pushHistoryQueries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { items ->
                items.map { pushHistory ->
                    PushHistoryItem(
                        pushDate = pushHistory.pushDate,
                        formattedDate = dateFormatter.format(
                            timestamp = pushHistory.pushDate,
                            mode = DateFormatterMode.Full,
                            useRelative = false,
                        ),
                        providerInfo = pushHistory.providerInfo,
                        eventId = pushHistory.eventId?.let { EventId(it) },
                        roomId = pushHistory.roomId?.let { RoomId(it) },
                        sessionId = pushHistory.sessionId?.let { SessionId(it) },
                        hasBeenResolved = pushHistory.hasBeenResolved == 1L,
                        comment = pushHistory.comment,
                    )
                }
            }
    }

    override suspend fun reset() {
        pushDatabase.pushHistoryQueries.removeAll()
        dataStore.edit {
            it.clear()
        }
    }

    companion object {
        const val BATTERY_OPTIMIZATION_BANNER_STATE_INIT = 0
        const val BATTERY_OPTIMIZATION_BANNER_STATE_SHOW = 1
        const val BATTERY_OPTIMIZATION_BANNER_STATE_DISMISSED = 2
    }
}
