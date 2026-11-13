/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.echo.libraries.matrix.impl.auth

import com.echo.features.enterprise.api.EnterpriseService
import com.echo.features.enterprise.test.FakeEnterpriseService
import com.echo.libraries.featureflag.test.FakeFeatureFlagService
import com.echo.libraries.matrix.api.paths.SessionPaths
import com.echo.libraries.matrix.impl.ClientBuilderProvider
import com.echo.libraries.matrix.impl.FakeClientBuilderProvider
import com.echo.libraries.matrix.impl.auth.qrlogin.SdkQrCodeLoginData
import com.echo.libraries.matrix.impl.createRustMatrixClientFactory
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiClientBuilder
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiHomeserverLoginDetails
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiLoginWithQrCodeHandler
import com.echo.libraries.matrix.impl.fixtures.fakes.FakeFfiQrCodeData
import com.echo.libraries.matrix.impl.paths.SessionPathsFactory
import com.echo.libraries.matrix.impl.storage.FakeSqliteStoreBuilder
import com.echo.libraries.matrix.impl.storage.FakeSqliteStoreBuilderProvider
import com.echo.libraries.matrix.impl.storage.SqliteStoreBuilder
import com.echo.libraries.matrix.impl.storage.SqliteStoreBuilderProvider
import com.echo.libraries.matrix.test.A_HOMESERVER_URL
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.auth.FakeOAuthRedirectUrlProvider
import com.echo.libraries.matrix.test.core.aBuildMeta
import com.echo.libraries.sessionstorage.api.SessionStore
import com.echo.libraries.sessionstorage.test.InMemorySessionStore
import com.echo.libraries.workmanager.test.FakeWorkManagerScheduler
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.testCoroutineDispatchers
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.HumanQrLoginException
import java.io.File

class RustMatrixAuthenticationServiceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `setHomeserver is successful`() = runTest {
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeClientBuilderProvider(
                provideResult = {
                    FakeFfiClientBuilder(
                        buildResult = {
                            FakeFfiClient(
                                homeserverLoginDetailsResult = {
                                    FakeFfiHomeserverLoginDetails()
                                }
                            )
                        }
                    )
                }
            ),
        )
        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()
    }

    @Test
    fun `setHomeserver can fail gracefully and clean up the temporary client`() = runTest {
        val closeResult = lambdaRecorder<Unit> {}
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeClientBuilderProvider(
                provideResult = {
                    FakeFfiClientBuilder(
                        buildResult = {
                            FakeFfiClient(
                                homeserverLoginDetailsResult = {
                                    throw IllegalStateException("Failed to get homeserver login details")
                                },
                                closeResult = closeResult,
                            )
                        },
                    )
                },
            ),
        )
        assertThat(sut.setHomeserver("matrix.org").isFailure).isTrue()
        closeResult.assertions().isCalledOnce()
    }

    @Test
    fun `login closes the client used to log in before building the client of the session`() = runTest {
        val events = mutableListOf<String>()
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeSequentialClientBuilderProvider(
                {
                    events.add("build login client")
                    FakeFfiClient(
                        homeserverLoginDetailsResult = { FakeFfiHomeserverLoginDetails() },
                        loginResult = { _, _ -> },
                        closeResult = { events.add("close login client") },
                    )
                },
                {
                    events.add("build session client")
                    FakeFfiClient(withUtdHook = {})
                },
            ),
        )

        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()
        assertThat(sut.login("alice", "password").getOrNull()).isEqualTo(A_USER_ID)

        // The two clients share the same session paths, so the login one must be closed first.
        assertThat(events).containsExactly("build login client", "close login client", "build session client").inOrder()
    }

    @Test
    fun `loginWithQrCode closes the client used to log in before building the client of the session`() = runTest {
        val events = mutableListOf<String>()
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeSequentialClientBuilderProvider(
                {
                    events.add("build login client")
                    FakeFfiClient(
                        newLoginWithQrCodeHandlerResult = { FakeFfiLoginWithQrCodeHandler() },
                        closeResult = { events.add("close login client") },
                    )
                },
                {
                    events.add("build session client")
                    FakeFfiClient(withUtdHook = {})
                },
            ),
        )

        val result = sut.loginWithQrCode(aSdkQrCodeLoginData()) {}

        assertThat(result.getOrNull()).isEqualTo(A_USER_ID)
        assertThat(events).containsExactly("build login client", "close login client", "build session client").inOrder()
    }

    @Test
    fun `loginWithQrCode closes the client it created when the login fails`() = runTest {
        val closeResult = lambdaRecorder<Unit> {}
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeClientBuilderProvider(
                provideResult = {
                    FakeFfiClientBuilder(
                        buildResult = {
                            FakeFfiClient(
                                newLoginWithQrCodeHandlerResult = {
                                    FakeFfiLoginWithQrCodeHandler(
                                        scanResult = { throw HumanQrLoginException.Unknown() },
                                    )
                                },
                                closeResult = closeResult,
                            )
                        },
                    )
                },
            ),
        )

        assertThat(sut.loginWithQrCode(aSdkQrCodeLoginData()) {}.isFailure).isTrue()
        closeResult.assertions().isCalledOnce()
    }

    @Test
    fun `loginWithQrCode cancels the login task when it is cancelled`() = runTest {
        val cancelResult = lambdaRecorder<Unit> {}
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeClientBuilderProvider(
                provideResult = {
                    FakeFfiClientBuilder(
                        buildResult = {
                            FakeFfiClient(
                                newLoginWithQrCodeHandlerResult = {
                                    FakeFfiLoginWithQrCodeHandler(
                                        scanResult = { awaitCancellation() },
                                        cancelResult = cancelResult,
                                    )
                                },
                                closeResult = {},
                            )
                        },
                    )
                },
            ),
        )

        val job = backgroundScope.launch {
            sut.loginWithQrCode(aSdkQrCodeLoginData()) {}
        }
        runCurrent()
        cancelResult.assertions().isNeverCalled()
        job.cancel()
        runCurrent()
        cancelResult.assertions().isCalledOnce()
    }

    private fun aSdkQrCodeLoginData() = SdkQrCodeLoginData(
        FakeFfiQrCodeData(
            baseUrlResult = { A_HOMESERVER_URL },
        )
    )

    /**
     * A [ClientBuilderProvider] handing out one [Client] per call, in order.
     */
    private class FakeSequentialClientBuilderProvider(
        private vararg val clients: () -> Client,
    ) : ClientBuilderProvider {
        private var index = 0
        override fun provide(): ClientBuilder = FakeFfiClientBuilder(buildResult = clients[index++])
    }

    @Test
    fun `a new login does not delete the session data of the account which has just been logged in`() = runTest {
        val storeBuilderProvider = SessionDirectoryCreatingSqliteStoreBuilderProvider()
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeSequentialClientBuilderProvider(
                { aLoginFakeFfiClient() },
                { FakeFfiClient(withUtdHook = {}) },
                { aLoginFakeFfiClient() },
            ),
            sessionPathsFactory = SessionPathsFactory(temporaryFolder.newFolder("base"), temporaryFolder.newFolder("cache")),
            sqliteStoreBuilderProvider = storeBuilderProvider,
        )

        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()
        assertThat(sut.login("alice", "password").getOrNull()).isEqualTo(A_USER_ID)
        val loggedInSessionPaths = storeBuilderProvider.providedSessionPaths.first()

        // Adding another account rotates the session paths, which must not touch the previous account.
        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()

        assertThat(loggedInSessionPaths.fileDirectory.exists()).isTrue()
        assertThat(loggedInSessionPaths.cacheDirectory.exists()).isTrue()
    }

    @Test
    fun `a new login deletes the session data of a previous failed login attempt`() = runTest {
        val storeBuilderProvider = SessionDirectoryCreatingSqliteStoreBuilderProvider()
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeSequentialClientBuilderProvider(
                { aLoginFakeFfiClient() },
                { throw IllegalStateException("Failed to build the client of the session") },
                { aLoginFakeFfiClient() },
            ),
            sessionPathsFactory = SessionPathsFactory(temporaryFolder.newFolder("base"), temporaryFolder.newFolder("cache")),
            sqliteStoreBuilderProvider = storeBuilderProvider,
        )

        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()
        assertThat(sut.login("alice", "password").isFailure).isTrue()
        val abandonedSessionPaths = storeBuilderProvider.providedSessionPaths.first()

        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()

        assertThat(abandonedSessionPaths.fileDirectory.exists()).isFalse()
        assertThat(abandonedSessionPaths.cacheDirectory.exists()).isFalse()
    }

    private fun aLoginFakeFfiClient() = FakeFfiClient(
        homeserverLoginDetailsResult = { FakeFfiHomeserverLoginDetails() },
        loginResult = { _, _ -> },
    )

    /**
     * A [SqliteStoreBuilderProvider] which creates the session directories, like the SDK does when it
     * opens its stores, and records them so that a test can assert on their lifecycle.
     */
    private class SessionDirectoryCreatingSqliteStoreBuilderProvider : SqliteStoreBuilderProvider {
        val providedSessionPaths = mutableListOf<SessionPaths>()

        override fun provide(sessionPaths: SessionPaths): SqliteStoreBuilder {
            sessionPaths.fileDirectory.mkdirs()
            sessionPaths.cacheDirectory.mkdirs()
            providedSessionPaths.add(sessionPaths)
            return FakeSqliteStoreBuilder()
        }
    }

    private fun TestScope.createRustMatrixAuthenticationService(
        sessionStore: SessionStore = InMemorySessionStore(updateUserProfileResult = { _, _, _, _ -> }),
        clientBuilderProvider: ClientBuilderProvider = FakeClientBuilderProvider(),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
        sessionPathsFactory: SessionPathsFactory = SessionPathsFactory(File("/base"), File("/cache")),
        sqliteStoreBuilderProvider: SqliteStoreBuilderProvider = FakeSqliteStoreBuilderProvider(),
    ): RustMatrixAuthenticationService {
        val rustMatrixClientFactory = createRustMatrixClientFactory(
            cacheDirectory = File("/cache"),
            sessionStore = sessionStore,
            clientBuilderProvider = clientBuilderProvider,
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}, cancelLambda = { _, _ -> }),
            sqliteStoreBuilderProvider = sqliteStoreBuilderProvider,
        )
        return RustMatrixAuthenticationService(
            sessionPathsFactory = sessionPathsFactory,
            coroutineDispatchers = testCoroutineDispatchers(),
            sessionStore = sessionStore,
            rustMatrixClientFactory = rustMatrixClientFactory,
            secretGenerator = FakeSecretGenerator(),
            oAuthConfigurationProvider = OAuthConfigurationProvider(
                buildMeta = aBuildMeta(),
                oAuthRedirectUrlProvider = FakeOAuthRedirectUrlProvider(),
            ),
            enterpriseService = enterpriseService,
            featureFlagService = FakeFeatureFlagService(),
            clientEnterpriseHook = {},
        )
    }
}
