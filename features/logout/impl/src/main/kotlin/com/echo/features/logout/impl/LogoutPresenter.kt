/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.logout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.architecture.runCatchingUpdatingState
import com.echo.libraries.core.bool.orTrue
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.encryption.BackupState
import com.echo.libraries.matrix.api.encryption.BackupUploadState
import com.echo.libraries.matrix.api.encryption.EncryptionService
import com.echo.libraries.workmanager.api.WorkManagerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Inject
class LogoutPresenter(
    private val matrixClient: MatrixClient,
    private val encryptionService: EncryptionService,
    private val workManagerScheduler: WorkManagerScheduler,
) : Presenter<LogoutState> {
    @Composable
    override fun present(): LogoutState {
        val localCoroutineScope = rememberCoroutineScope()
        val currentUser by matrixClient.userProfile.collectAsState()
        val logoutAction: MutableState<AsyncAction<Unit>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }

        val backupUploadState: BackupUploadState by remember {
            encryptionService.waitForBackupUploadSteadyState()
        }
            .collectAsState(initial = BackupUploadState.Unknown)

        var waitingForALongTime by remember { mutableStateOf(false) }
        LaunchedEffect(backupUploadState) {
            if (backupUploadState is BackupUploadState.Waiting) {
                delay(2_000)
                waitingForALongTime = true
            } else {
                waitingForALongTime = false
            }
        }

        val isLastDevice by encryptionService.isLastDevice.collectAsState()
        val backupState by encryptionService.backupStateStateFlow.collectAsState()
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()

        val doesBackupExistOnServerAction: MutableState<AsyncData<Boolean>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        LaunchedEffect(backupState) {
            if (backupState == BackupState.UNKNOWN) {
                getKeyBackupStatus(doesBackupExistOnServerAction)
            }
        }

        fun handleEvent(event: LogoutEvent) {
            when (event) {
                is LogoutEvent.Logout -> {
                    if (logoutAction.value.isConfirming() || event.ignoreSdkError) {
                        localCoroutineScope.logout(logoutAction, event.ignoreSdkError)
                    } else {
                        logoutAction.value = AsyncAction.ConfirmingNoParams
                    }
                }
                LogoutEvent.CloseDialogs -> {
                    logoutAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return LogoutState(
            currentUser = currentUser,
            isLastDevice = isLastDevice,
            backupState = backupState,
            doesBackupExistOnServer = doesBackupExistOnServerAction.value.dataOrNull().orTrue(),
            recoveryState = recoveryState,
            backupUploadState = backupUploadState,
            waitingForALongTime = waitingForALongTime,
            logoutAction = logoutAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.getKeyBackupStatus(action: MutableState<AsyncData<Boolean>>) = launch {
        suspend {
            encryptionService.doesBackupExistOnServer().getOrThrow()
        }.runCatchingUpdatingState(action)
    }

    private fun CoroutineScope.logout(
        logoutAction: MutableState<AsyncAction<Unit>>,
        ignoreSdkError: Boolean,
    ) = launch {
        suspend {
            // Cancel any pending work (e.g. notification sync)
            workManagerScheduler.cancel(matrixClient.sessionId)

            matrixClient.logout(userInitiated = true, ignoreSdkError)
        }.runCatchingUpdatingState(logoutAction)
    }
}

