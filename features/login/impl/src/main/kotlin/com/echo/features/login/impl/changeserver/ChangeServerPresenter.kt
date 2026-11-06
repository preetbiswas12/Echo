/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.changeserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import com.echo.features.login.impl.accesscontrol.DefaultAccountProviderAccessControl
import com.echo.features.login.impl.accountprovider.AccountProviderDataSource
import com.echo.features.login.impl.error.ChangeServerError
import com.echo.features.login.impl.localnetwork.LocalNetworkPermissionGate
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.architecture.runCatchingUpdatingState
import com.echo.libraries.matrix.api.accountprovider.AccountProvider
import com.echo.libraries.matrix.api.auth.MatrixAuthenticationService
import com.echo.libraries.matrix.api.auth.MatrixHomeServerDetails

@Inject
class ChangeServerPresenter(
    private val authenticationService: MatrixAuthenticationService,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val defaultAccountProviderAccessControl: DefaultAccountProviderAccessControl,
    private val localNetworkPermissionGate: LocalNetworkPermissionGate,
) : Presenter<ChangeServerState> {
    @Composable
    override fun present(): ChangeServerState {
        val changeServerAction: MutableState<AsyncData<MatrixHomeServerDetails>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        val gateState = localNetworkPermissionGate.present<AccountProvider>(
            urlOf = { provider -> provider.serverNameOrBaseUrl() },
            onProceed = { provider -> changeServer(provider, changeServerAction) },
        )

        fun handleEvent(event: ChangeServerEvent) {
            when (event) {
                ChangeServerEvent.ClearError -> changeServerAction.value = AsyncData.Uninitialized
                is ChangeServerEvent.ChangeServer -> gateState.submit(event.accountProvider)
                ChangeServerEvent.DismissLocalNetworkPermission -> gateState.abort()
                ChangeServerEvent.RequestLocalNetworkPermission -> gateState.requestPermission()
            }
        }

        return ChangeServerState(
            changeServerAction = changeServerAction.value,
            localNetworkPermissionDialog = gateState.dialog,
            eventSink = ::handleEvent,
        )
    }

    private suspend fun changeServer(
        data: AccountProvider,
        changeServerAction: MutableState<AsyncData<MatrixHomeServerDetails>>,
    ) {
        suspend {
            defaultAccountProviderAccessControl.assertIsAllowedToConnectToAccountProvider(data)
            val details = authenticationService.setHomeserver(data.serverNameOrBaseUrl()).getOrThrow()
            if (!details.isSupported) {
                throw ChangeServerError.UnsupportedServer
            }
            // Homeserver is valid, remember user choice
            accountProviderDataSource.setAccountProvider(data)
            // Return the resolved details so the caller can sign in without configuring the homeserver again.
            details
        }.runCatchingUpdatingState(changeServerAction, errorTransform = ChangeServerError::from)
    }
}

