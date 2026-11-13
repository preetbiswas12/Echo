/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.lockscreen.impl.setup.biometric

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.features.lockscreen.impl.R
import com.echo.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import com.echo.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import com.echo.libraries.designsystem.atomic.pages.HeaderFooterPage
import com.echo.libraries.designsystem.components.BigIcon
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.Button
import com.echo.libraries.designsystem.theme.components.TextButton

@Composable
fun SetupBiometricView(
    state: SetupBiometricState,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        state.eventSink(SetupBiometricEvent.UsePin)
    }
    HeaderFooterPage(
        modifier = modifier.padding(top = 80.dp),
        header = {
            SetupBiometricHeader()
        },
        footer = {
            SetupBiometricFooter(
                onAllowClick = { state.eventSink(SetupBiometricEvent.AllowBiometric) },
                onSkipClick = { state.eventSink(SetupBiometricEvent.UsePin) }
            )
        },
    )
}

@Composable
private fun SetupBiometricHeader() {
    val biometricAuth = stringResource(id = R.string.screen_app_lock_biometric_authentication)
    IconTitleSubtitleMolecule(
        iconStyle = BigIcon.Style.Default(Icons.Default.Fingerprint),
        title = stringResource(id = R.string.screen_app_lock_settings_enable_biometric_unlock),
        subTitle = stringResource(id = R.string.screen_app_lock_setup_biometric_unlock_subtitle, biometricAuth),
    )
}

@Composable
private fun SetupBiometricFooter(
    onAllowClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    ButtonColumnMolecule {
        val biometricAuth = stringResource(id = R.string.screen_app_lock_biometric_authentication)
        Button(
            text = stringResource(id = R.string.screen_app_lock_setup_biometric_unlock_allow_title, biometricAuth),
            onClick = onAllowClick
        )
        TextButton(
            text = stringResource(id = R.string.screen_app_lock_setup_biometric_unlock_skip),
            onClick = onSkipClick
        )
    }
}

@Composable
@PreviewsDayNight
internal fun SetupBiometricViewPreview(@PreviewParameter(SetupBiometricStatePreviewParam::class) state: SetupBiometricState) {
    ElementPreview {
        SetupBiometricView(
            state = state,
        )
    }
}
