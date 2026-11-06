/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.root

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import com.echo.annotations.ContributesNode
import com.echo.compound.theme.ElementTheme
import com.echo.features.preferences.impl.account.PreferencesAccountCallback
import com.echo.libraries.androidutils.browser.openUrlInChromeCustomTab
import com.echo.libraries.architecture.callback
import com.echo.libraries.di.SessionScope
import com.echo.libraries.emoji.api.picker.EmojiPickerRenderer
import com.echo.libraries.matrix.api.core.SessionId

@ContributesNode(SessionScope::class)
@AssistedInject
class PreferencesRootNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PreferencesRootPresenter,
    private val emojiPickerRenderer: EmojiPickerRenderer,
) : Node(buildContext, plugins = plugins) {
    interface Callback : PreferencesAccountCallback, Plugin {
        fun navigateToAddAccount()
        fun navigateToSession(sessionId: SessionId)
        fun navigateToAnalyticsSettings()
        fun navigateToAbout()
        fun navigateToDeveloperSettings()
        fun navigateToLockScreenSettings()
        fun navigateToMediaSettings()
        fun navigateToLocationSettings()
        fun navigateToLabs()
    }

    private val callback: Callback = callback()

    private fun onManageAccountClick(
        activity: Activity,
        url: String?,
        isDark: Boolean,
    ) {
        url?.let {
            activity.openUrlInChromeCustomTab(
                null,
                darkTheme = isDark,
                url = it
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()

        PreferencesRootView(
            state = state,
            emojiPickerRenderer = emojiPickerRenderer,
            modifier = modifier,
            onBackClick = this::navigateUp,
            onAddAccountClick = callback::navigateToAddAccount,
            onSwitchToSession = callback::navigateToSession,
            onOpenAnalytics = callback::navigateToAnalyticsSettings,
            onOpenAbout = callback::navigateToAbout,
            onOpenDeveloperSettings = callback::navigateToDeveloperSettings,
            onOpenMediaSettings = callback::navigateToMediaSettings,
            onOpenLocationSettings = callback::navigateToLocationSettings,
            onOpenLabs = callback::navigateToLabs,
            onOpenLockScreenSettings = callback::navigateToLockScreenSettings,
            onOpenRageShake = callback::navigateToBugReport,
            onSecureBackupClick = callback::navigateToSecureBackup,
            onModerationAndSafetyClick = callback::navigateToModerationAndSafety,
            onLinkNewDeviceClick = callback::navigateToLinkNewDevice,
            onManageAccountClick = { onManageAccountClick(activity, it, isDark) },
            onOpenNotificationSettings = callback::navigateToNotificationSettings,
            onEditProfileClick = callback::navigateToUserProfile,
            onSignOutClick = callback::startSignOutFlow,
            onDeactivateClick = callback::startAccountDeactivationFlow
        )
    }
}

