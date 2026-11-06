/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.test

import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.push.api.PushService
import com.echo.libraries.push.api.history.PushHistoryItem
import com.echo.libraries.pushproviders.api.Distributor
import com.echo.libraries.pushproviders.api.PushProvider
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePushService(
    private val testPushBlock: suspend (SessionId) -> Boolean = { true },
    private val availablePushProviders: List<PushProvider> = emptyList(),
    private val registerWithLambda: (MatrixClient, PushProvider, Distributor) -> Result<Unit> = { _, _, _ ->
        Result.success(Unit)
    },
    private val currentPushProvider: (SessionId) -> PushProvider? = { availablePushProviders.firstOrNull() },
    private val selectPushProviderLambda: suspend (SessionId, PushProvider) -> Unit = { _, _ -> lambdaError() },
    private val setIgnoreRegistrationErrorLambda: (SessionId, Boolean) -> Unit = { _, _ -> lambdaError() },
    private val resetPushHistoryResult: () -> Unit = { lambdaError() },
    private val resetBatteryOptimizationStateResult: () -> Unit = { lambdaError() },
    private val onServiceUnregisteredResult: (UserId) -> Unit = { lambdaError() },
    private val ensurePusherIsRegisteredResult: () -> Result<Unit> = { lambdaError() },
) : PushService {
    override suspend fun getCurrentPushProvider(sessionId: SessionId): PushProvider? {
        return registeredPushProvider ?: currentPushProvider(sessionId)
    }

    override fun getAvailablePushProviders(): List<PushProvider> {
        return availablePushProviders
    }

    private var registeredPushProvider: PushProvider? = null

    override suspend fun registerWith(
        matrixClient: MatrixClient,
        pushProvider: PushProvider,
        distributor: Distributor,
    ): Result<Unit> = simulateLongTask {
        return registerWithLambda(matrixClient, pushProvider, distributor)
            .also {
                if (it.isSuccess) {
                    registeredPushProvider = pushProvider
                }
            }
    }

    override suspend fun ensurePusherIsRegistered(matrixClient: MatrixClient): Result<Unit> {
        return ensurePusherIsRegisteredResult()
    }

    override suspend fun selectPushProvider(sessionId: SessionId, pushProvider: PushProvider) {
        selectPushProviderLambda(sessionId, pushProvider)
    }

    private val ignoreRegistrationError = MutableStateFlow(false)

    override fun ignoreRegistrationError(sessionId: SessionId): Flow<Boolean> {
        return ignoreRegistrationError
    }

    override suspend fun setIgnoreRegistrationError(sessionId: SessionId, ignore: Boolean) {
        ignoreRegistrationError.value = ignore
        setIgnoreRegistrationErrorLambda(sessionId, ignore)
    }

    override suspend fun testPush(sessionId: SessionId): Boolean = simulateLongTask {
        testPushBlock(sessionId)
    }

    private val pushHistoryItemsFlow = MutableStateFlow<List<PushHistoryItem>>(emptyList())

    override fun getPushHistoryItemsFlow(): Flow<List<PushHistoryItem>> {
        return pushHistoryItemsFlow
    }

    fun emitPushHistoryItems(items: List<PushHistoryItem>) {
        pushHistoryItemsFlow.value = items
    }

    private val pushCounterFlow = MutableStateFlow(0)

    override val pushCounter: Flow<Int> = pushCounterFlow

    fun emitPushCounter(counter: Int) {
        pushCounterFlow.value = counter
    }

    override suspend fun resetPushHistory() = simulateLongTask {
        resetPushHistoryResult()
    }

    override suspend fun resetBatteryOptimizationState() {
        resetBatteryOptimizationStateResult()
    }

    override suspend fun onServiceUnregistered(userId: UserId) {
        onServiceUnregisteredResult(userId)
    }
}

