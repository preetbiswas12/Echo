/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.createaccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.echo.features.login.impl.accountprovider.AccountProviderDataSource
import com.echo.features.login.impl.accountprovider.SaveAccountProviderToHistory
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.matrix.api.auth.MatrixAuthenticationService
import com.echo.libraries.matrix.api.core.SessionId
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch

@AssistedInject
class CreateAccountPresenter(
    private val authenticationService: MatrixAuthenticationService,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val saveAccountProviderToHistory: SaveAccountProviderToHistory,
) : Presenter<CreateAccountState> {
    @AssistedFactory
    interface Factory {
        fun create(): CreateAccountPresenter
    }

    @Composable
    override fun present(): CreateAccountState {
        val localCoroutineScope = rememberCoroutineScope()
        val registerAction: MutableState<AsyncData<SessionId>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        val formState = rememberSaveable {
            mutableStateOf(CreateAccountFormState.Default)
        }
        val accountProvider by accountProviderDataSource.flow.collectAsState()

        fun handleEvent(event: CreateAccountEvent) {
            when (event) {
                is CreateAccountEvent.SetUsername -> updateFormState(formState) {
                    copy(username = event.username)
                }
                is CreateAccountEvent.SetPassword -> updateFormState(formState) {
                    copy(password = event.password)
                }
                is CreateAccountEvent.SetConfirmedPassword -> updateFormState(formState) {
                    copy(confirmedPassword = event.password)
                }
                is CreateAccountEvent.SetRegistrationCode -> updateFormState(formState) {
                    copy(registrationCode = event.registrationCode)
                }
                CreateAccountEvent.Submit -> {
                    localCoroutineScope.launch {
                        registerAction.value = AsyncData.Loading()
                        authenticationService.register(
                            username = formState.value.username.lowercase(),
                            password = formState.value.password,
                            registrationCode = formState.value.registrationCode.takeIf { it.isNotBlank() },
                        )
                            .onSuccess { sessionId ->
                                saveAccountProviderToHistory()
                                registerAction.value = AsyncData.Success(sessionId)
                            }
                            .onFailure { failure ->
                                registerAction.value = AsyncData.Failure(failure)
                            }
                    }
                }
                CreateAccountEvent.ClearError -> registerAction.value = AsyncData.Uninitialized
            }
        }

        return CreateAccountState(
            accountProvider = accountProvider,
            formState = formState.value,
            registerAction = registerAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun updateFormState(
        formState: MutableState<CreateAccountFormState>,
        updateLambda: CreateAccountFormState.() -> CreateAccountFormState,
    ) {
        formState.value = updateLambda(formState.value)
    }
}
