/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.securebackup.impl.reset

import dev.zacsweers.metro.Inject
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.di.annotations.SessionCoroutineScope
import com.echo.libraries.matrix.api.encryption.BackupState
import com.echo.libraries.matrix.api.encryption.EncryptionService
import com.echo.libraries.matrix.api.encryption.IdentityResetHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Inject
class ResetIdentityFlowManager(
    private val encryptionService: EncryptionService,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    private val resetHandleFlow: MutableStateFlow<AsyncData<IdentityResetHandle?>> = MutableStateFlow(AsyncData.Uninitialized)
    val currentHandleFlow: StateFlow<AsyncData<IdentityResetHandle?>> = resetHandleFlow
    private var whenResetIsDoneWaitingJob: Job? = null

    fun whenResetIsDone(block: () -> Unit) {
        whenResetIsDoneWaitingJob = sessionCoroutineScope.launch {
            encryptionService.backupStateStateFlow.first { it == BackupState.ENABLED }
            block()
        }
    }

    fun getResetHandle(): StateFlow<AsyncData<IdentityResetHandle?>> {
        return if (resetHandleFlow.value.isLoading() || resetHandleFlow.value.isSuccess()) {
            resetHandleFlow
        } else {
            resetHandleFlow.value = AsyncData.Loading()

            sessionCoroutineScope.launch {
                encryptionService.startIdentityReset()
                    .onSuccess { handle ->
                        resetHandleFlow.value = AsyncData.Success(handle)
                    }
                    .onFailure {
                        resetHandleFlow.value = AsyncData.Failure(it)
                    }
            }

            resetHandleFlow
        }
    }

    suspend fun cancel() {
        currentHandleFlow.value.dataOrNull()?.cancel()
        resetHandleFlow.value = AsyncData.Uninitialized

        whenResetIsDoneWaitingJob?.cancel()
        whenResetIsDoneWaitingJob = null
    }
}

