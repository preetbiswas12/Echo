/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.confirmaccountprovider

import app.cash.turbine.ReceiveTurbine
import com.echo.features.enterprise.api.EnterpriseService
import com.echo.features.enterprise.test.FakeEnterpriseService
import com.echo.features.login.impl.accesscontrol.DefaultAccountProviderAccessControl
import com.echo.features.login.impl.accountprovider.AccountProviderDataSource
import com.echo.features.login.impl.accountprovider.anAccountProviderDataSource
import com.echo.features.login.impl.changeserver.ChangeServerPresenter
import com.echo.features.login.impl.localnetwork.LocalNetworkPermissionGate
import com.echo.features.login.impl.login.LoginMode
import com.echo.features.login.impl.screens.createaccount.AccountCreationNotSupported
import com.echo.features.login.impl.screens.onboarding.createLoginModePresenter
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.matrix.api.auth.MatrixAuthenticationService
import com.echo.libraries.matrix.test.AN_EXCEPTION
import com.echo.libraries.matrix.test.accountprovider.anAccountProviderManaged
import com.echo.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import com.echo.libraries.matrix.test.auth.aMatrixHomeServerDetails
import com.echo.libraries.oauth.api.OAuthAction
import com.echo.libraries.oauth.api.OAuthActionFlow
import com.echo.libraries.oauth.test.FakeOAuthActionFlow
import com.echo.libraries.permissions.test.FakeLocalNetworkPermissionAdvisor
import com.echo.libraries.permissions.test.FakePermissionsPresenter
import com.echo.libraries.permissions.test.FakePermissionsPresenterFactory
import com.echo.libraries.preferences.api.store.AppPreferencesStore
import com.echo.libraries.preferences.test.InMemoryAppPreferencesStore
import com.echo.tests.testutils.WarmUpRule
import com.echo.tests.testutils.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ConfirmAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    companion object {
        private const val MATRIX_ORG_SERVER_NAME = "matrix.org"
        private const val MATRIX_ORG_URL = "https://matrix.org"
        private const val ECHO_SERVER_NAME = "echo-server-a4ae.onrender.com"
    }

    @Test
    fun `present - initial test`() = runTest {
        val presenter = createConfirmAccountProviderPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.isAccountCreation).isFalse()
            assertThat(initialState.submitEnabled).isTrue()
            assertThat(initialState.accountProviderInput).isEqualTo(ECHO_SERVER_NAME)
            assertThat(initialState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - continue password login`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isEqualTo(LoginMode.PasswordLogin)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - continue oidc`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - cancel with failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOAuthActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOAuthActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            authenticationService.givenOAuthCancelError(AN_EXCEPTION)
            defaultOAuthActionFlow.post(OAuthAction.GoBack())
            val cancelFailureState = awaitLoginMode { it is AsyncData.Failure }
            assertThat(cancelFailureState.loginModeState.loginMode).isInstanceOf(AsyncData.Failure::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - cancel with success`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOAuthActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOAuthActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            defaultOAuthActionFlow.post(OAuthAction.GoBack())
            val cancelFinalState = awaitLoginMode { it is AsyncData.Uninitialized }
            assertThat(cancelFinalState.loginModeState.loginMode).isInstanceOf(AsyncData.Uninitialized::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - cancel to unblock`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOAuthActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOAuthActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            awaitLoginMode { it is AsyncData.Loading }
            defaultOAuthActionFlow.post(OAuthAction.GoBack(toUnblock = true))
            val cancelFinalState = awaitLoginMode { it is AsyncData.Uninitialized }
            assertThat(cancelFinalState.loginModeState.loginMode).isInstanceOf(AsyncData.Uninitialized::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - success with failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOAuthActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOAuthActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            authenticationService.givenLoginError(AN_EXCEPTION)
            defaultOAuthActionFlow.post(OAuthAction.Success("aUrl"))
            val cancelFailureState = awaitLoginMode { it is AsyncData.Failure }
            assertThat(cancelFailureState.loginModeState.loginMode).isInstanceOf(AsyncData.Failure::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - success with success`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOidcActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOidcActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            defaultOidcActionFlow.post(OAuthAction.Success("aUrl"))
            val successSuccessState = awaitLoginMode { it is AsyncData.Loading }
            assertThat(successSuccessState.loginModeState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - submit fails`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            // The account provider validation fails, so the login is never attempted.
            val failureState = awaitState { it.changeServerState.changeServerAction is AsyncData.Failure }
            assertThat(failureState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()

            // Submit will return an error while validating the account provider
            initialState.eventSink(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))

            // Check an error was returned
            val submittedState = awaitState { it.changeServerState.changeServerAction is AsyncData.Failure }

            // Assert the error is then cleared
            submittedState.eventSink(ConfirmAccountProviderEvent.ClearError)
            val clearedState = awaitState { it.changeServerState.changeServerAction is AsyncData.Uninitialized }
            assertThat(clearedState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - confirm account creation without oidc generates an error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            params = ConfirmAccountProviderPresenter.Params(isAccountCreation = true),
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            // Check an error was returned
            val submittedState = awaitLoginMode { it is AsyncData.Failure }
            assertThat(submittedState.loginModeState.loginMode.errorOrNull()).isInstanceOf(AccountCreationNotSupported::class.java)
            // Assert the error is then cleared
            submittedState.eventSink(ConfirmAccountProviderEvent.ClearError)
            val clearedState = awaitLoginMode { it is AsyncData.Uninitialized }
            assertThat(clearedState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - confirm account creation with OAuth is successful`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            params = ConfirmAccountProviderPresenter.Params(isAccountCreation = true),
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            val submittedState = awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - offers an autocomplete suggestion from the account provider history`() = runTest {
        val presenter = createConfirmAccountProviderPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(
                homeserverHistory = listOf("https://randomcommunity.org"),
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviderSuggestion).isNull()
            initialState.eventSink(ConfirmAccountProviderEvent.UserInputChanged("random"))
            val suggestionState = awaitState { it.accountProviderInput == "random" }
            // Completion is offered without the https:// scheme, even though history stores the full URL.
            assertThat(suggestionState.accountProviderSuggestion).isEqualTo("randomcommunity.org")
        }
    }

    @Test
    fun `present - continue accepts the autocomplete suggestion rather than the typed prefix`() = runTest {
        val submittedUrls = mutableListOf<String>()
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { url ->
                submittedUrls.add(url)
                Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            appPreferencesStore = InMemoryAppPreferencesStore(
                homeserverHistory = listOf("https://randomcommunity.org"),
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.UserInputChanged("random"))
            val suggestionState = awaitState { it.accountProviderSuggestion != null }
            suggestionState.eventSink(ConfirmAccountProviderEvent.Continue("randomcommunity.org"))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedUrls.first()).isEqualTo("randomcommunity.org")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - continue applies the completed account provider back into the field`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true)) },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            appPreferencesStore = InMemoryAppPreferencesStore(homeserverHistory = listOf("https://randomcommunity.org")),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.UserInputChanged("random"))
            val suggestionState = awaitState { it.accountProviderSuggestion != null }
            suggestionState.eventSink(ConfirmAccountProviderEvent.Continue("randomcommunity.org"))
            // The field now renders the full accepted server rather than the typed prefix.
            val appliedState = awaitState { it.accountProviderInput == "randomcommunity.org" }
            assertThat(appliedState.accountProviderInput).isEqualTo("randomcommunity.org")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - offers the enterprise allow-list servers as autocomplete suggestions`() = runTest {
        val presenter = createConfirmAccountProviderPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(homeserverHistory = emptyList()),
            enterpriseService = FakeEnterpriseService(
                accountProviderAllowListResult = { listOf(anAccountProviderManaged(serverName = "element.io")) },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.UserInputChanged("ele"))
            val suggestionState = awaitState { it.accountProviderInput == "ele" }
            assertThat(suggestionState.accountProviderSuggestion).isEqualTo("element.io")
        }
    }

    @Test
    fun `present - offers the previously used servers without the scheme as autocomplete suggestions`() = runTest {
        val presenter = createConfirmAccountProviderPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(homeserverHistory = listOf("https://previous.org")),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.UserInputChanged("prev"))
            val suggestionState = awaitState { it.accountProviderInput == "prev" }
            assertThat(suggestionState.accountProviderSuggestion).isEqualTo("previous.org")
        }
    }

    @Test
    fun `present - offers the Echo homeserver without the scheme as an autocomplete suggestion`() = runTest {
        val presenter = createConfirmAccountProviderPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(homeserverHistory = emptyList()),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.UserInputChanged("echo"))
            val suggestionState = awaitState { it.accountProviderInput == "echo" }
            assertThat(suggestionState.accountProviderSuggestion).isEqualTo(ECHO_SERVER_NAME)
        }
    }

    @Test
    fun `present - does not autocomplete a scheme-prefixed input`() = runTest {
        // Candidates are bare hosts, so a scheme-prefixed input never completes them - the user completes by
        // typing the bare host. This also avoids ever suggesting an insecure http scheme.
        val presenter = createConfirmAccountProviderPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(homeserverHistory = emptyList()),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.UserInputChanged("https://mat"))
            assertThat(awaitState { it.accountProviderInput == "https://mat" }.accountProviderSuggestion).isNull()
            initialState.eventSink(ConfirmAccountProviderEvent.UserInputChanged("http://mat"))
            assertThat(awaitState { it.accountProviderInput == "http://mat" }.accountProviderSuggestion).isNull()
        }
    }

    @Test
    fun `present - continue prepends https to a bare account provider`() = runTest {
        val submittedUrls = mutableListOf<String>()
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { url ->
                submittedUrls.add(url)
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.Continue("matrix.org"))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedUrls.first()).isEqualTo(MATRIX_ORG_SERVER_NAME)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - continue configures the homeserver only once`() = runTest {
        // The consolidated Continue flow validates the account provider and then signs in. Login reuses the
        // details resolved during validation, so setHomeserver runs exactly once rather than once per phase.
        val submittedUrls = mutableListOf<String>()
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { url ->
                submittedUrls.add(url)
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.Continue(MATRIX_ORG_URL))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedUrls).containsExactly(MATRIX_ORG_SERVER_NAME)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - continue with a full matrix id signs in to its homeserver and passes a login hint`() = runTest {
        val submittedUrls = mutableListOf<String>()
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { url ->
                submittedUrls.add(url)
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(matrixAuthenticationService = authenticationService)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.Continue("@alice:example.org"))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedUrls.first()).isEqualTo("example.org")
            assertThat(authenticationService.getOAuthUrlLoginHint).isEqualTo("mxid:@alice:example.org")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - continue with a matrix id keeps the port in the homeserver`() = runTest {
        val submittedUrls = mutableListOf<String>()
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { url ->
                submittedUrls.add(url)
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(matrixAuthenticationService = authenticationService)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.Continue("@alice:example.org:8448"))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedUrls.first()).isEqualTo("example.org:8448")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - continue with a plain account provider passes no login hint`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true)) },
        )
        val presenter = createConfirmAccountProviderPresenter(matrixAuthenticationService = authenticationService)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.Continue("matrix.org"))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(authenticationService.getOAuthUrlLoginHint).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - continue with a matrix id during account creation passes no login hint`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true)) },
        )
        val presenter = createConfirmAccountProviderPresenter(
            params = ConfirmAccountProviderPresenter.Params(isAccountCreation = true),
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvent.Continue("@alice:example.org"))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(authenticationService.getOAuthUrlLoginHint).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Awaits until the emitted state's login mode matches [predicate], skipping the intermediate
     * account-provider validation states, and returns that state.
     */
    private suspend fun ReceiveTurbine<ConfirmAccountProviderState>.awaitLoginMode(
        predicate: (AsyncData<LoginMode>) -> Boolean,
    ): ConfirmAccountProviderState = awaitState { predicate(it.loginModeState.loginMode) }

    private suspend fun ReceiveTurbine<ConfirmAccountProviderState>.awaitState(
        predicate: (ConfirmAccountProviderState) -> Boolean,
    ): ConfirmAccountProviderState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    private fun createConfirmAccountProviderPresenter(
        params: ConfirmAccountProviderPresenter.Params = ConfirmAccountProviderPresenter.Params(isAccountCreation = false),
        accountProviderDataSource: AccountProviderDataSource = anAccountProviderDataSource(),
        matrixAuthenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
        defaultOAuthActionFlow: OAuthActionFlow = FakeOAuthActionFlow(),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
    ) = ConfirmAccountProviderPresenter(
        params = params,
        accountProviderDataSource = accountProviderDataSource,
        appPreferencesStore = appPreferencesStore,
        enterpriseService = enterpriseService,
        loginModePresenter = createLoginModePresenter(
            authenticationService = matrixAuthenticationService,
            oAuthActionFlow = defaultOAuthActionFlow,
        ),
        changeServerPresenter = ChangeServerPresenter(
            authenticationService = matrixAuthenticationService,
            accountProviderDataSource = accountProviderDataSource,
            defaultAccountProviderAccessControl = DefaultAccountProviderAccessControl(
                enterpriseService = FakeEnterpriseService(
                    isAllowedToConnectToAccountProviderResult = { true },
                    isElementProEnforcedResult = { false },
                ),
                isEnterpriseBuild = { false },
            ),
            localNetworkPermissionGate = LocalNetworkPermissionGate(
                advisor = FakeLocalNetworkPermissionAdvisor(),
                permissionsPresenterFactory = FakePermissionsPresenterFactory(FakePermissionsPresenter()),
            ),
        ),
    )
}
