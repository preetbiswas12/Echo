/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.loginpassword

import com.echo.features.login.impl.accountprovider.AccountProviderDataSource
import com.echo.features.login.impl.accountprovider.SaveAccountProviderToHistory
import com.echo.features.login.impl.accountprovider.anAccountProviderDataSource
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.matrix.api.accountprovider.matrixOrgAccountProvider
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.test.AN_EXCEPTION
import com.echo.libraries.matrix.test.A_PASSWORD
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.matrix.test.A_USER_NAME
import com.echo.libraries.matrix.test.A_USER_NAME_2
import com.echo.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import com.echo.libraries.matrix.test.auth.aMatrixHomeServerDetails
import com.echo.libraries.preferences.api.store.AppPreferencesStore
import com.echo.libraries.preferences.test.InMemoryAppPreferencesStore
import com.echo.tests.testutils.WarmUpRule
import com.echo.tests.testutils.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LoginPasswordPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createLoginPasswordPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.accountProvider).isEqualTo(matrixOrgAccountProvider)
            assertThat(initialState.formState).isEqualTo(LoginFormState.Default)
            assertThat(initialState.loginAction).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.submitEnabled).isFalse()
        }
    }

    @Test
    fun `present - initial login is in the first state and can be modified`() = runTest {
        createLoginPasswordPresenter(
            initialLogin = A_USER_NAME,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.formState.login).isEqualTo(A_USER_NAME)
            // Login can be changed
            initialState.eventSink.invoke(LoginPasswordEvent.SetLogin(A_USER_NAME_2))
            val loginChangedState = awaitItem()
            assertThat(loginChangedState.formState.login).isEqualTo(A_USER_NAME_2)
        }
    }

    @Test
    fun `present - enter login and password`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails())
            },
        )
        createLoginPasswordPresenter(
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvent.SetLogin(A_USER_NAME))
            val loginState = awaitItem()
            assertThat(loginState.formState).isEqualTo(LoginFormState(login = A_USER_NAME, password = ""))
            assertThat(loginState.submitEnabled).isFalse()
            initialState.eventSink.invoke(LoginPasswordEvent.SetPassword(A_PASSWORD))
            val loginAndPasswordState = awaitItem()
            assertThat(loginAndPasswordState.formState).isEqualTo(LoginFormState(login = A_USER_NAME, password = A_PASSWORD))
            assertThat(loginAndPasswordState.submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - submit`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails())
            },
        )
        createLoginPasswordPresenter(
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvent.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvent.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvent.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(AsyncData.Loading::class.java)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loginAction).isEqualTo(AsyncData.Success(A_SESSION_ID))
        }
    }

    @Test
    fun `present - successful login saves the account provider to history`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails())
            },
        )
        val appPreferencesStore = InMemoryAppPreferencesStore()
        createLoginPasswordPresenter(
            authenticationService = authenticationService,
            appPreferencesStore = appPreferencesStore,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvent.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvent.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvent.Submit)
            skipItems(1)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loginAction).isEqualTo(AsyncData.Success(A_SESSION_ID))
            assertThat(appPreferencesStore.getHomeserverHistoryFlow().first())
                .containsExactly(matrixOrgAccountProvider.serverName)
        }
    }

    @Test
    fun `present - submit with error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails())
            },
        )
        createLoginPasswordPresenter(
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvent.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvent.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            authenticationService.givenLoginError(AN_EXCEPTION)
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvent.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(AsyncData.Loading::class.java)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loginAction).isEqualTo(AsyncData.Failure<SessionId>(AN_EXCEPTION))
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails())
            },
        )
        createLoginPasswordPresenter(
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvent.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvent.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            authenticationService.givenLoginError(AN_EXCEPTION)
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvent.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(AsyncData.Loading::class.java)
            val loggedInState = awaitItem()
            // Check an error was returned
            assertThat(loggedInState.loginAction).isEqualTo(AsyncData.Failure<SessionId>(AN_EXCEPTION))
            // Assert the error is then cleared
            loggedInState.eventSink(LoginPasswordEvent.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginAction).isEqualTo(AsyncData.Uninitialized)
        }
    }

    private fun createLoginPasswordPresenter(
        initialLogin: String = "",
        authenticationService: FakeMatrixAuthenticationService = FakeMatrixAuthenticationService(),
        accountProviderDataSource: AccountProviderDataSource = anAccountProviderDataSource(),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    ): LoginPasswordPresenter = LoginPasswordPresenter(
        initialLogin = initialLogin,
        authenticationService = authenticationService,
        accountProviderDataSource = accountProviderDataSource,
        saveAccountProviderToHistory = SaveAccountProviderToHistory(accountProviderDataSource, appPreferencesStore),
    )
}
