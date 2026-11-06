/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import com.echo.compound.theme.Theme
import com.echo.compound.theme.mapToTheme
import com.echo.features.location.api.LocationService
import com.echo.features.preferences.impl.account.PreferencesAccountState
import com.echo.features.preferences.impl.userstatus.UserStatusState
import com.echo.features.preferences.impl.utils.ShowDeveloperSettingsProvider
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import com.echo.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import com.echo.libraries.di.annotations.SessionCoroutineScope
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.MatrixClientProvider
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.user.toMatrixUser
import com.echo.libraries.matrix.ui.components.switchaccount.OtherAccount
import com.echo.libraries.preferences.api.store.AppPreferencesStore
import com.echo.libraries.sessionstorage.api.SessionData
import com.echo.libraries.sessionstorage.api.SessionStore
import com.echo.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class PreferencesRootPresenter(
    private val matrixClient: MatrixClient,
    private val analyticsService: AnalyticsService,
    private val versionFormatter: VersionFormatter,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val showDeveloperSettingsProvider: ShowDeveloperSettingsProvider,
    private val featureFlagService: FeatureFlagService,
    private val locationService: LocationService,
    private val sessionStore: SessionStore,
    private val matrixClientProvider: MatrixClientProvider,
    private val appPreferencesStore: AppPreferencesStore,
    private val userStatusPresenter: Presenter<UserStatusState>,
    private val preferencesAccountPresenter: Presenter<PreferencesAccountState>,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<PreferencesRootState> {
    @Composable
    override fun present(): PreferencesRootState {
        val coroutineScope = rememberCoroutineScope()
        val matrixUser = matrixClient.userProfile.collectAsState()
        LaunchedEffect(Unit) {
            // Force a refresh of the profile
            matrixClient.getUserProfile()
        }
        val preferencesAccountState = preferencesAccountPresenter.present()
        val isMultiAccountEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.MultiAccount)
        }.collectAsState(initial = false)
        val isOtherAccountsSectionExpanded by remember {
            appPreferencesStore.isOtherAccountsExpandedFlow()
        }.collectAsState(initial = true)
        val isUserStatusSupported by produceState(false) {
            value = matrixClient.isUserStatusSupported().getOrDefault(false)
        }
        val userStatusState = if (isUserStatusSupported) userStatusPresenter.present() else null
        val isBlackThemeAllowed by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.AllowBlackTheme)
        }.collectAsState(initial = false)
        val theme = remember(isBlackThemeAllowed) {
            appPreferencesStore.getThemeFlow().mapToTheme(isBlackThemeAllowed)
        }.collectAsState(initial = Theme.System)

        val otherSessions by remember {
            sessionStore.sessionsFlow()
                .map { list -> list.filter { it.userId != matrixClient.sessionId.value } }
                .flatMapLatest { sessions ->
                    if (sessions.isEmpty()) {
                        flowOf(persistentListOf())
                    } else {
                        combine(
                            sessions.map { sessionData -> otherAccountFlow(sessionData) }
                        ) { it.toImmutableList() }
                    }
                }
        }.collectAsState(initial = persistentListOf())

        val themeOption by remember {
            derivedStateOf {
                when (theme.value) {
                    Theme.System -> ThemeOption.System
                    Theme.Dark -> ThemeOption.Dark
                    Theme.Black -> ThemeOption.Black
                    Theme.Light -> ThemeOption.Light
                }
            }
        }

        val availableThemeOptions = remember(isBlackThemeAllowed) {
            if (isBlackThemeAllowed) {
                ThemeOption.entries
            } else {
                ThemeOption.entries.filterNot { it == ThemeOption.Black }
            }.toImmutableList()
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        val hasAnalyticsProviders = remember { analyticsService.getAvailableAnalyticsProviders().isNotEmpty() }

        val showLabsItem = remember { featureFlagService.getAvailableFeatures(isInLabs = true).isNotEmpty() }

        val showDeveloperSettings by showDeveloperSettingsProvider.showDeveloperSettings.collectAsState()

        val isLocationServiceAvailable by remember { locationService.isServiceAvailable() }.collectAsState()

        fun handleEvent(event: PreferencesRootEvent) {
            when (event) {
                is PreferencesRootEvent.OnVersionInfoClick -> {
                    showDeveloperSettingsProvider.unlockDeveloperSettings(coroutineScope)
                }
                PreferencesRootEvent.ToggleOtherAccountsExpanded -> coroutineScope.launch {
                    appPreferencesStore.setOtherAccountsExpanded(!isOtherAccountsSectionExpanded)
                }
                is PreferencesRootEvent.SetTheme -> sessionCoroutineScope.launch {
                    when (event.theme) {
                        ThemeOption.System -> appPreferencesStore.setTheme(Theme.System.name)
                        ThemeOption.Dark -> appPreferencesStore.setTheme(Theme.Dark.name)
                        ThemeOption.Black -> appPreferencesStore.setTheme(Theme.Black.name)
                        ThemeOption.Light -> appPreferencesStore.setTheme(Theme.Light.name)
                    }
                }
            }
        }

        return PreferencesRootState(
            myUser = matrixUser.value,
            userStatusState = userStatusState,
            preferencesAccountState = preferencesAccountState,
            theme = themeOption,
            availableThemeOptions = availableThemeOptions,
            version = remember { versionFormatter.get() },
            isMultiAccountEnabled = isMultiAccountEnabled,
            isOtherAccountsSectionExpanded = isOtherAccountsSectionExpanded,
            otherSessions = otherSessions,
            showAnalyticsSettings = hasAnalyticsProviders,
            showDeveloperSettings = showDeveloperSettings,
            showLabsItem = showLabsItem,
            showLocationSettings = isLocationServiceAvailable,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvent,
        )
    }

    /**
     * Build the [OtherAccount] data for a session which is not the current one.
     */
    private fun otherAccountFlow(sessionData: SessionData): Flow<OtherAccount> = flow {
        val user = sessionData.toMatrixUser()
        val client = matrixClientProvider.getOrNull(SessionId(sessionData.userId))
        if (client == null) {
            emit(OtherAccount(user = user, unreadCount = 0))
        } else {
            emitAll(
                combine(client.userProfile, client.totalUnreadCountFlow) { profile, unreadCount ->
                    // Don't use userProfile directly so we don't lose avatarThumbnail
                    OtherAccount(
                        user = user.copy(displayedStatus = profile.displayedStatus, rawStatus = profile.rawStatus),
                        unreadCount = unreadCount.toInt(),
                    )
                }
            )
        }
    }
}

