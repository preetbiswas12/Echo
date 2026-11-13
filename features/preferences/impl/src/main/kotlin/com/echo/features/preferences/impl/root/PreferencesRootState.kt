/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.echo.features.preferences.impl.R
import com.echo.features.preferences.impl.account.PreferencesAccountState
import com.echo.features.preferences.impl.userstatus.UserStatusState
import com.echo.libraries.designsystem.components.preferences.DropdownOption
import com.echo.libraries.designsystem.utils.snackbar.SnackbarMessage
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.matrix.ui.components.switchaccount.OtherAccount
import kotlinx.collections.immutable.ImmutableList

data class PreferencesRootState(
    val myUser: MatrixUser,
    val theme: ThemeOption,
    val preferencesAccountState: PreferencesAccountState,
    val availableThemeOptions: ImmutableList<ThemeOption>,
    val userStatusState: UserStatusState?,
    val version: String,
    val isMultiAccountEnabled: Boolean,
    val isOtherAccountsSectionExpanded: Boolean,
    val otherSessions: ImmutableList<OtherAccount>,
    val showAnalyticsSettings: Boolean,
    val showDeveloperSettings: Boolean,
    val showLabsItem: Boolean,
    val showLocationSettings: Boolean,
    val snackbarMessage: SnackbarMessage?,
    val eventSink: (PreferencesRootEvent) -> Unit,
)

enum class ThemeOption : DropdownOption {
    System {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_system)
    },

    Light {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_light)
    },

    Dark {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_dark)
    },

    Black {
        @Composable
        @ReadOnlyComposable
        override fun getText(): String = stringResource(R.string.theme_black)
    }
}
