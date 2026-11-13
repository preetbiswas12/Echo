/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.classic.missingkeybackup

import androidx.compose.runtime.Composable
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.core.meta.BuildMeta
import dev.zacsweers.metro.Inject

@Inject
class MissingKeyBackupPresenter(
    private val buildMeta: BuildMeta,
) : Presenter<MissingKeyBackupState> {
    @Composable
    override fun present(): MissingKeyBackupState {
        return MissingKeyBackupState(
            appName = buildMeta.applicationName,
        )
    }
}
