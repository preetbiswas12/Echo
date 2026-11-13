/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.enterprise.impl

import androidx.compose.ui.graphics.Color
import com.echo.compound.colors.SemanticColorsLightDark
import com.echo.features.enterprise.api.BugReportUrl
import com.echo.features.enterprise.api.EnterpriseService
import com.echo.libraries.androidutils.json.JsonProvider
import com.echo.libraries.core.coroutine.CoroutineDispatchers
import com.echo.libraries.core.extensions.mapCatchingExceptions
import com.echo.libraries.core.uri.ensureProtocol
import com.echo.libraries.matrix.api.ClientUrlContentFetcher
import com.echo.libraries.matrix.api.TemporaryMatrixClientFactory
import com.echo.libraries.matrix.api.accountprovider.AccountProvider
import com.echo.libraries.matrix.api.core.SessionId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber

@ContributesBinding(AppScope::class)
class DefaultEnterpriseService(
    private val temporaryMatrixClientFactory: TemporaryMatrixClientFactory,
    private val jsonProvider: JsonProvider,
    private val dispatchers: CoroutineDispatchers,
) : EnterpriseService {
    override suspend fun isEnterpriseUser(sessionId: SessionId) = false
    override suspend fun tweakMasUrl(url: String, urlContentFetcher: ClientUrlContentFetcher) = url
    override fun accountProviderAllowList(): List<AccountProvider> = emptyList()
    override fun canConnectToAnyAccountProvider(): Boolean = true
    override suspend fun isAllowedToConnectToAccountProvider(accountProvider: AccountProvider) = true
    override suspend fun isElementProEnforced(serverName: String): Boolean = withContext(dispatchers.io) {
        val temporaryMatrixClient = temporaryMatrixClientFactory.create(serverName).getOrElse { return@withContext false }
        temporaryMatrixClient.use { client ->
            val baseUrl = serverName.ensureProtocol().removeSuffix("/")
            // We'll always perform a network request here since we're not interested in any cached value.
            client.getUrl("$baseUrl/.well-known/element/element.json")
                .mapCatchingExceptions { response ->
                    val remoteConfig = jsonProvider().decodeFromString<MinimalEnterpriseConfig>(String(response))
                    remoteConfig.enforceElementPro ?: false
                }
                .onFailure {
                    Timber.e(it, "Failed to fetch enterprise config for checking if Element Pro is enforced for $serverName")
                }
                .getOrElse { false }
        }
    }

    override suspend fun overrideBrandColor(sessionId: SessionId?, brandColor: String?) = Unit

    override fun brandColorsFlow(sessionId: SessionId?): Flow<Color?> {
        return flowOf(null)
    }

    override fun semanticColorsFlow(sessionId: SessionId?): Flow<SemanticColorsLightDark> {
        return flowOf(SemanticColorsLightDark.default)
    }

    override fun firebasePushGateway(): String? = null
    override fun unifiedPushDefaultPushGateway(): String? = null

    override fun bugReportUrlFlow(sessionId: SessionId?): Flow<BugReportUrl> {
        return flowOf(BugReportUrl.UseDefault)
    }

    override fun getNoisyNotificationChannelId(sessionId: SessionId): String? = null
}

/**
 * A minimal version of the enterprise config that only contains the fields we need to check if Element Pro is enforced.
 */
@Serializable
private data class MinimalEnterpriseConfig(
    @SerialName("enforce_element_pro") val enforceElementPro: Boolean? = null,
)
