/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.chooseaccountprovider

import com.google.common.truth.Truth.assertThat
import com.echo.features.enterprise.api.EnterpriseService
import com.echo.features.enterprise.test.FakeEnterpriseService
import com.echo.features.login.impl.login.LoginModePresenter
import com.echo.features.login.impl.screens.onboarding.createLoginModePresenter
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.matrix.test.AN_ACCOUNT_PROVIDER_2
import com.echo.libraries.matrix.test.AN_ACCOUNT_PROVIDER_3
import com.echo.libraries.matrix.test.AN_EXCEPTION
import com.echo.libraries.matrix.test.accountprovider.anAccountProviderManaged
import com.echo.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import com.echo.tests.testutils.WarmUpRule
import com.echo.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChooseAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    companion object {
        private const val ACCOUNT_PROVIDER_FROM_CONFIG_1 = AN_ACCOUNT_PROVIDER_2
        private const val ACCOUNT_PROVIDER_FROM_CONFIG_2 = AN_ACCOUNT_PROVIDER_3
        val accountProvider1 = anAccountProviderManaged(serverName = ACCOUNT_PROVIDER_FROM_CONFIG_1)
        val accountProvider2 = anAccountProviderManaged(serverName = ACCOUNT_PROVIDER_FROM_CONFIG_2)
    }

    @Test
    fun `present - ensure initial conditions`() {
        assertThat(
            setOf(
                ACCOUNT_PROVIDER_FROM_CONFIG_1,
                ACCOUNT_PROVIDER_FROM_CONFIG_2,
            ).size
        ).isEqualTo(2)
    }

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter(
            enterpriseService = FakeEnterpriseService(
                accountProviderAllowListResult = { listOf(accountProvider1, accountProvider2) },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).containsExactly(
                accountProvider1,
                accountProvider2,
            )
            assertThat(initialState.selectedAccountProvider).isNull()
        }
    }

    @Test
    fun `present - Continue when no account provider is selected has no effect`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = createPresenter(
            enterpriseService = FakeEnterpriseService(
                accountProviderAllowListResult = { listOf(accountProvider1, accountProvider2) },
            ),
            loginModePresenter = createLoginModePresenter(
                authenticationService = authenticationService,
            ),
        )
        presenter.test {
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isNull()
                it.eventSink(ChooseAccountProviderEvent.Continue)
                expectNoEvents()
            }
        }
    }

    @Test
    fun `present - select account provider and continue - error then clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val presenter = createPresenter(
            enterpriseService = FakeEnterpriseService(
                accountProviderAllowListResult = { listOf(accountProvider1, accountProvider2) },
            ),
            loginModePresenter = createLoginModePresenter(
                authenticationService = authenticationService,
            ),
        )
        presenter.test {
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isNull()
                it.eventSink(ChooseAccountProviderEvent.SelectAccountProvider(accountProvider1))
            }
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isEqualTo(accountProvider1)
                it.eventSink(ChooseAccountProviderEvent.Continue)
                skipItems(1) // Loading

                // Check an error was returned
                val submittedState = awaitItem()
                assertThat(submittedState.loginModeState.loginMode).isInstanceOf(AsyncData.Failure::class.java)

                // Assert the error is then cleared
                submittedState.eventSink(ChooseAccountProviderEvent.ClearError)
                val clearedState = awaitItem()
                assertThat(clearedState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
            }
        }
    }

    @Test
    fun `present - default account provider - select account provider during login has no effect`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = createPresenter(
            enterpriseService = FakeEnterpriseService(
                accountProviderAllowListResult = { listOf(accountProvider1, accountProvider2) },
            ),
            loginModePresenter = createLoginModePresenter(
                authenticationService = authenticationService,
            ),
        )
        presenter.test {
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isNull()
                it.eventSink(ChooseAccountProviderEvent.SelectAccountProvider(accountProvider1))
            }
            awaitItem().also {
                assertThat(it.selectedAccountProvider).isEqualTo(accountProvider1)
                it.eventSink(ChooseAccountProviderEvent.Continue)
            }
            awaitItem().also {
                assertThat(it.loginModeState.loginMode.isLoading()).isTrue()
                it.eventSink(ChooseAccountProviderEvent.SelectAccountProvider(accountProvider2))
            }
            expectNoEvents()
        }
    }
}

private fun createPresenter(
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
    loginModePresenter: LoginModePresenter = createLoginModePresenter(),
) = ChooseAccountProviderPresenter(
    enterpriseService = enterpriseService,
    loginModePresenter = loginModePresenter,
)

