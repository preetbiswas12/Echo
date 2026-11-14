/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.auth

import com.echo.features.enterprise.api.ClientEnterpriseHook
import com.echo.features.enterprise.api.EnterpriseService
import com.echo.libraries.androidutils.crypto.ClientSecret
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.core.extensions.mapFailure
import com.echo.libraries.core.extensions.runCatchingExceptions
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.auth.AuthenticationException
import com.echo.libraries.matrix.api.auth.ElementClassicSession
import com.echo.libraries.matrix.api.auth.MatrixAuthenticationService
import com.echo.libraries.matrix.api.auth.MatrixHomeServerDetails
import com.echo.libraries.matrix.api.auth.OAuthDetails
import com.echo.libraries.matrix.api.auth.OAuthPrompt
import com.echo.libraries.matrix.api.auth.RegistrationException
import com.echo.libraries.matrix.api.auth.SessionRestorationException
import com.echo.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import com.echo.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.paths.SessionPaths
import com.echo.libraries.matrix.api.verification.SessionVerifiedStatus
import com.echo.libraries.matrix.impl.ClientBuilderSlidingSync
import com.echo.libraries.matrix.impl.NoOpClientSessionDelegate
import com.echo.libraries.matrix.impl.RustMatrixClientFactory
import com.echo.libraries.matrix.impl.RustTemporaryMatrixClient
import com.echo.libraries.matrix.impl.auth.qrlogin.QrErrorMapper
import com.echo.libraries.matrix.impl.auth.qrlogin.SdkQrCodeLoginData
import com.echo.libraries.matrix.impl.auth.qrlogin.toStep
import com.echo.libraries.matrix.impl.exception.mapClientException
import com.echo.libraries.matrix.impl.keys.SecretGenerator
import com.echo.libraries.matrix.impl.mapper.toSessionData
import com.echo.libraries.matrix.impl.paths.SessionPathsFactory
import com.echo.libraries.sessionstorage.api.LoginType
import com.echo.libraries.sessionstorage.api.SessionData
import com.echo.libraries.sessionstorage.api.SessionStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.HumanQrLoginException
import org.matrix.rustcomponents.sdk.QrCodeData
import org.matrix.rustcomponents.sdk.QrCodeDecodeException
import org.matrix.rustcomponents.sdk.QrLoginProgress
import org.matrix.rustcomponents.sdk.QrLoginProgressListener
import org.matrix.rustcomponents.sdk.SecretsBundleWithUserId
import timber.log.Timber
import uniffi.matrix_sdk.OAuthAuthorizationData
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RustMatrixAuthenticationService(
    private val sessionPathsFactory: SessionPathsFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    private val rustMatrixClientFactory: RustMatrixClientFactory,
    private val secretGenerator: SecretGenerator,
    private val oAuthConfigurationProvider: OAuthConfigurationProvider,
    private val enterpriseService: EnterpriseService,
    private val featureFlagService: FeatureFlagService,
    private val clientEnterpriseHook: ClientEnterpriseHook,
) : MatrixAuthenticationService {
    // Any existing Element Classic session that we want to try to import secrets from during login.
    private var elementClassicSession: ElementClassicSession? = null

    // Passphrase which will be used for new sessions. Existing sessions will use the passphrase
    // stored in the SessionData.
    private val pendingKey by lazy { getDatabaseKey() }

    // Need to keep a copy of the current session path to eventually delete it.
    // Ideally it would be possible to get the sessionPath from the Client to avoid doing this.
    private var sessionPaths: SessionPaths? = null
    private var currentClient: Client? = null

    private val newMatrixClientObservers = mutableListOf<(MatrixClient) -> Unit>()
    override fun listenToNewMatrixClients(lambda: (MatrixClient) -> Unit) {
        newMatrixClientObservers.add(lambda)
    }

    private fun rotateSessionPath(): SessionPaths {
        sessionPaths?.deleteRecursively()
        return sessionPathsFactory.create()
            .also { sessionPaths = it }
    }

    override suspend fun restoreSession(sessionId: SessionId): Result<MatrixClient> = withContext(coroutineDispatchers.io) {
        runCatchingExceptions {
            val sessionData = sessionStore.getSession(sessionId.value)
            if (sessionData != null) {
                restoreSession(sessionData)
            } else {
                throw SessionRestorationException.MissingSession(sessionId)
            }
        }.mapFailure { failure ->
            failure.mapClientException()
        }
    }

    private suspend fun restoreSession(sessionData: SessionData): MatrixClient {
        if (!sessionData.isTokenValid) {
            throw SessionRestorationException.InvalidToken()
        }
        // Use the sessionData.passphrase, which can be null for a previously created session
        if (sessionData.passphrase == null) {
            Timber.w("Restoring a session without a passphrase")
        } else {
            Timber.w("Restoring a session with a passphrase")
        }
        return rustMatrixClientFactory.create(sessionData)
    }

    private fun getDatabaseKey(): ClientSecret {
        Timber.d("New sessions will be encrypted with a raw key")
        return secretGenerator.generateKey()
    }

    override suspend fun setHomeserver(homeserver: String): Result<MatrixHomeServerDetails> =
        withContext(coroutineDispatchers.io) {
            val emptySessionPath = rotateSessionPath()
            runCatchingExceptions {
                val client = makeClient(sessionPaths = emptySessionPath) {
                    serverNameOrHomeserverUrl(homeserver)
                }

                currentClient = client

                client.homeserverLoginDetails().map()
            }.onFailure {
                clear()
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to set homeserver to $homeserver")
                failure.mapAuthenticationException()
            }
        }

    override suspend fun login(username: String, password: String): Result<SessionId> =
        withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val currentSessionPaths = sessionPaths ?: error("You need to call `setHomeserver()` first")
                client.login(
                    username = username,
                    password = password,
                    initialDeviceName = "Echo Android",
                    deviceId = null,
                )
                // Ensure that the user is not already logged in with the same account
                ensureNotAlreadyLoggedIn(client)
                tryToImportSecretForElementClassicSession(client)
                val sessionData = client.session()
                    .toSessionData(
                        isTokenValid = true,
                        loginType = LoginType.PASSWORD,
                        passphrase = pendingKey.formattedAsString(),
                        sessionPaths = currentSessionPaths,
                    )
                finalizeClientCreation(sessionData)
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to login")
                failure.mapAuthenticationException()
            }
        }

    override suspend fun register(
        username: String,
        password: String,
        registrationCode: String?,
    ): Result<SessionId> =
        withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val currentSessionPaths = sessionPaths ?: error("You need to call `setHomeserver()` first")
                val homeserverUrl = client.homeserver()
                val registration = createAccount(
                    homeserverUrl = homeserverUrl,
                    username = username.trim(),
                    password = password,
                    registrationCode = registrationCode?.takeIf { it.isNotBlank() },
                )
                val accountAlreadyLoggedIn = sessionStore.getAllSessions().any {
                    it.userId == registration.userId
                }
                if (accountAlreadyLoggedIn) {
                    throw AuthenticationException.AccountAlreadyLoggedIn(registration.userId)
                }
                // Mirror `login()`: build the final session from the credentials of the new account. The
                // passphrase must be the pending key, since the local store of the client above was
                // already created with it.
                val sessionData = SessionData(
                    userId = registration.userId,
                    deviceId = registration.deviceId,
                    accessToken = registration.accessToken,
                    refreshToken = null,
                    homeserverUrl = homeserverUrl,
                    oAuthData = null,
                    loginTimestamp = Date(),
                    isTokenValid = true,
                    loginType = LoginType.PASSWORD,
                    passphrase = pendingKey.formattedAsString(),
                    sessionPath = currentSessionPaths.fileDirectory.absolutePath,
                    cachePath = currentSessionPaths.cacheDirectory.absolutePath,
                    position = 0,
                    lastUsageIndex = 0,
                    userDisplayName = null,
                    userAvatarUrl = null,
                    userAvatarData = null,
                )
                finalizeClientCreation(sessionData)
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to register")
                failure as? RegistrationException ?: failure.mapAuthenticationException()
            }
        }

    /**
     * The result of a successful `POST /register` request, with the credentials of the freshly created
     * account.
     */
    private data class AccountRegistration(
        val userId: String,
        val deviceId: String,
        val accessToken: String,
    )

    /**
     * Creates the account on the homeserver with a plain `POST /register` request.
     *
     * This is not going through the Rust SDK, which has no API for password registration. The returned
     * credentials are then handed to the SDK to build the final session.
     */
    private fun createAccount(
        homeserverUrl: String,
        username: String,
        password: String,
        registrationCode: String?,
    ): AccountRegistration {
        val body = buildJsonObject {
            put("username", username)
            put("password", password)
            put("initial_device_display_name", "Echo Android")
            registrationCode?.let {
                putJsonObject("auth") {
                    put("type", "m.login.registration_token")
                    put("token", it)
                }
            }
        }.toString()

        val connection = URL(homeserverUrl.trimEnd('/') + "/_matrix/client/v3/register")
            .openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { it.write(body.encodeToByteArray()) }

            val stream = if (connection.errorStream != null && connection.responseCode >= 400) {
                connection.errorStream
            } else {
                connection.inputStream
            }
            val responseBody = stream?.bufferedReader()?.use { it.readText() } ?: ""
            val json = Json.parseToJsonElement(responseBody).jsonObject

            if (connection.responseCode >= 400) {
                val errcode = json["errcode"]?.jsonPrimitive?.content
                val error = json["error"]?.jsonPrimitive?.content
                Timber.d("Registration rejected by the server: errcode=$errcode")
                throw RegistrationException(errcode, error)
            }

            val userId = json["user_id"]?.jsonPrimitive?.content
            val accessToken = json["access_token"]?.jsonPrimitive?.content
            val deviceId = json["device_id"]?.jsonPrimitive?.content
            if (userId == null || accessToken == null || deviceId == null) {
                throw RegistrationException(null, "Malformed registration response from the server")
            }
            AccountRegistration(userId = userId, deviceId = deviceId, accessToken = accessToken)
        } catch (failure: IOException) {
            throw RegistrationException(null, "Could not reach the homeserver: ${failure.message}")
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun tryToImportSecretForElementClassicSession(client: Client) {
        elementClassicSession
            ?.takeIf {
                // Note: the SDK will also do this check
                it.userId.value == client.userId()
            }
            ?.let {
                val secrets = it.secrets
                val roomKeysVersion = it.roomKeysVersion
                if (secrets == null || roomKeysVersion == null) {
                    Timber.d("No secrets or roomKeysVersion found for Element Classic session ${it.userId}, skipping import")
                } else {
                    Timber.d("Trying to import secrets for Element Classic session ${it.userId}")
                    runCatchingExceptions {
                        SecretsBundleWithUserId.fromStr(
                            userId = it.userId.value,
                            bundle = secrets,
                            backupInfo = roomKeysVersion,
                        ).use { secretsBundle ->
                            client.encryption().importSecretsBundle(secretsBundle)
                        }
                    }.onFailure { failure ->
                        Timber.e(failure, "Failed to import secrets for Element Classic session ${it.userId}")
                    }
                }
            }
    }

    override fun doSecretsContainBackupKey(
        userId: UserId,
        secrets: String,
        backupInfo: String,
    ): Boolean {
        return try {
            SecretsBundleWithUserId.fromStr(
                userId = userId.value,
                bundle = secrets,
                backupInfo = backupInfo,
            ).use { secretsBundle ->
                secretsBundle.containsBackupKey()
            }
        } catch (failure: Exception) {
            Timber.e(failure, "Failed to parse secrets for Element Classic session $userId")
            false
        }
    }

    private var pendingOAuthAuthorizationData: OAuthAuthorizationData? = null

    override suspend fun getOAuthUrl(
        prompt: OAuthPrompt,
        loginHint: String?,
    ): Result<OAuthDetails> {
        return withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val oAuthAuthorizationData = client.urlForOauth(
                    oauthConfiguration = oAuthConfigurationProvider.get(),
                    prompt = prompt.toRustPrompt(),
                    loginHint = loginHint,
                    // If we want to restore a previous session for which we have encryption keys, we can pass the deviceId here. At the moment, we don't
                    deviceId = null,
                    additionalScopes = emptyList(),
                )
                val getUrlResolver = RustTemporaryMatrixClient(client, sessionPaths, coroutineDispatchers.io)
                val url = oAuthAuthorizationData.loginUrl()
                    .let {
                        enterpriseService.tweakMasUrl(
                            url = it,
                            urlContentFetcher = getUrlResolver,
                        )
                    }
                pendingOAuthAuthorizationData = oAuthAuthorizationData
                OAuthDetails(url)
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to get OAuth URL")
                failure.mapAuthenticationException()
            }
        }
    }

    override suspend fun cancelOAuthLogin(): Result<Unit> {
        return withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                pendingOAuthAuthorizationData?.use {
                    currentClient?.abortOauthAuth(it)
                }
                pendingOAuthAuthorizationData = null
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to cancel OAuth login")
                failure.mapAuthenticationException()
            }
        }
    }

    override fun setElementClassicSession(session: ElementClassicSession?) {
        elementClassicSession = session
    }

    /**
     * callbackUrl should be the `url` from `OAuthAction` (with all the parameters).
     */
    override suspend fun loginWithOAuth(callbackUrl: String): Result<SessionId> {
        return withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val currentSessionPaths = sessionPaths ?: error("You need to call `setHomeserver()` first")
                client.loginWithOauthCallback(
                    callbackUrl = callbackUrl,
                )
                // Free the pending data since we won't use it to abort the flow anymore
                pendingOAuthAuthorizationData?.close()
                pendingOAuthAuthorizationData = null
                // Ensure that the user is not already logged in with the same account
                ensureNotAlreadyLoggedIn(client)
                tryToImportSecretForElementClassicSession(client)
                val sessionData = client.session().toSessionData(
                    isTokenValid = true,
                    loginType = LoginType.OIDC,
                    passphrase = pendingKey.formattedAsString(),
                    sessionPaths = currentSessionPaths,
                )
                finalizeClientCreation(sessionData)
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to login with OAuth")
                failure.mapAuthenticationException()
            }
        }
    }

    @Throws(AuthenticationException.AccountAlreadyLoggedIn::class)
    private suspend fun ensureNotAlreadyLoggedIn(client: Client) {
        val newUserId = client.userId()
        val accountAlreadyLoggedIn = sessionStore.getAllSessions().any {
            it.userId == newUserId
        }
        if (accountAlreadyLoggedIn) {
            // Sign out the client, ignoring any error
            runCatchingExceptions {
                client.logout()
            }
            throw AuthenticationException.AccountAlreadyLoggedIn(newUserId)
        }
    }

    override suspend fun loginWithQrCode(qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit) =
        withContext(coroutineDispatchers.io) {
            val sdkQrCodeLoginData = (qrCodeData as SdkQrCodeLoginData).rustQrCodeData
            val emptySessionPaths = rotateSessionPath()
            val oAuthConfiguration = oAuthConfigurationProvider.get()
            val progressListener = object : QrLoginProgressListener {
                override fun onUpdate(state: QrLoginProgress) {
                    Timber.d("QR Code login progress: $state")
                    progress(state.toStep())
                }
            }
            runCatchingExceptions {
                val client = makeQrCodeLoginClient(
                    sessionPaths = emptySessionPaths,
                    qrCodeData = sdkQrCodeLoginData,
                )
                currentClient = client
                client.newLoginWithQrCodeHandler(
                    oauthConfiguration = oAuthConfiguration,
                ).use {
                    it.scan(
                        qrCodeData = qrCodeData.rustQrCodeData,
                        progressListener = progressListener,
                    ).use { task ->
                        try {
                            task.wait()
                        } catch (e: CancellationException) {
                            task.cancel()
                            throw e
                        }
                    }
                }
                // Ensure that the user is not already logged in with the same account
                ensureNotAlreadyLoggedIn(client)
                val sessionData = client.session()
                    .toSessionData(
                        isTokenValid = true,
                        loginType = LoginType.QR,
                        passphrase = pendingKey.formattedAsString(),
                        sessionPaths = emptySessionPaths,
                    )
                finalizeClientCreation(sessionData)
            }.mapFailure {
                when (it) {
                    is QrCodeDecodeException -> QrErrorMapper.map(it)
                    is HumanQrLoginException -> QrErrorMapper.map(it)
                    else -> it
                }
            }.onFailure { throwable ->
                // A QR code login always builds its own client, so it can be disposed of on failure.
                clear()
                if (throwable is CancellationException) {
                    throw throwable
                }
                Timber.e(throwable, "Failed to login with QR code")
            }
        }

    private suspend fun finalizeClientCreation(sessionData: SessionData): SessionId {
        // Close the client which was used to perform the login before creating the final client.
        // Both use the same session paths, so their SQLite stores must never be opened at the same time.
        // The login client does not set up the backup and the cross-signing, the final client will do it.
        clear()

        val matrixClient = restoreSession(sessionData)
        // Apply enterprise hooks to the newly created client as soon as possible
        clientEnterpriseHook(matrixClient)

        // Start the sync service to ensure that the client receives the encryption updates we'll wait for next.
        matrixClient.syncService.startSync()

        matrixClient.waitForKnownVerificationState()

        newMatrixClientObservers.forEach { it.invoke(matrixClient) }
        sessionStore.addSession(sessionData)

        // The session paths now hold the data of the account which has just been logged in, so forget
        // them: they must not be deleted by the rotateSessionPath() of the next login attempt.
        sessionPaths = null

        return SessionId(sessionData.userId)
    }

    private suspend fun makeClient(
        sessionPaths: SessionPaths,
        config: suspend ClientBuilder.() -> ClientBuilder,
    ): Client {
        Timber.d("Creating client with simplified sliding sync")
        return rustMatrixClientFactory
            .getBaseClientBuilder(
                sessionPaths = sessionPaths,
                clientSecret = pendingKey,
                slidingSyncType = ClientBuilderSlidingSync.Discovered,
                isMessageSearchAvailable = isMessageSearchAvailable(),
                sessionDelegate = NoOpClientSessionDelegate,
                autoEnableEncryption = false,
            )
            .config()
            .build()
    }

    private suspend fun makeQrCodeLoginClient(
        sessionPaths: SessionPaths,
        qrCodeData: QrCodeData,
    ): Client {
        Timber.d("Creating client for QR Code login with simplified sliding sync")
        // The 2025 version of MSC4108 provides baseUrl; the 2024 version has null baseUrl and uses
        // serverName instead, which can be null or malformed. We only enforce presence/non-blankness
        // here and rely on serverNameOrHomeserverUrl()/the Rust builder layer to validate structure.
        val baseUrlOrServerName = qrCodeData.baseUrl() ?: qrCodeData.serverName()

        if (baseUrlOrServerName == null) {
            // With the 2024 version of MSC4108 we treat the absence of serverName as meaning that
            // the other device is not signed in.
            Timber.e("The QR code is from a device that is not yet signed in")
            throw HumanQrLoginException.OtherDeviceNotSignedIn()
        }

        if (baseUrlOrServerName.isBlank()) {
            Timber.e("The QR code contains an empty base URL or server name, which is invalid")
            throw HumanQrLoginException.Unknown()
        }

        return rustMatrixClientFactory
            .getBaseClientBuilder(
                sessionPaths = sessionPaths,
                clientSecret = pendingKey,
                slidingSyncType = ClientBuilderSlidingSync.Discovered,
                isMessageSearchAvailable = isMessageSearchAvailable(),
                sessionDelegate = NoOpClientSessionDelegate,
                autoEnableEncryption = false,
            )
            .serverNameOrHomeserverUrl(baseUrlOrServerName)
            .build()
    }

    private fun clear() {
        currentClient?.close()
        currentClient = null
    }

    private suspend fun isMessageSearchAvailable(): Boolean =
        featureFlagService.isFeatureEnabled(FeatureFlags.MessageSearch)

    private suspend fun MatrixClient.waitForKnownVerificationState() {
        withTimeoutOrNull(10.seconds) {
            Timber.d("Waiting for a known verification status...")
            val status = sessionVerificationService.sessionVerifiedStatus.first { it != SessionVerifiedStatus.Unknown }
            Timber.d("Finished waiting for a known verification status: $status")
        } ?: Timber.w("Timed out waiting for a known verification status")
    }
}
