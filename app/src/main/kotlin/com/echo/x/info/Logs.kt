/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.x.info

import android.content.Context
import com.echo.libraries.androidutils.system.getVersionCodeFromManifest
import com.echo.libraries.core.log.logger.wrapInBox
import com.echo.BuildConfig
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun logApplicationInfo(context: Context, sdkGitSha: String) {
    val appVersion = "${BuildConfig.VERSION_NAME} (${context.getVersionCodeFromManifest()}) - ${BuildConfig.BUILD_TYPE} / ${BuildConfig.FLAVOR}"
    val date = SimpleDateFormat("MM-dd HH:mm:ss.SSSZ", Locale.US).format(Date())
    listOf(
        " Application version: $appVersion",
        " Echo      : https://github.com/preetbiswas12/Echo/commit/${BuildConfig.GIT_REVISION}",
        " SDK      : https://github.com/matrix-org/matrix-rust-sdk/commit/$sdkGitSha",
        " Local time: $date",
    )
        .wrapInBox(minBoxInsideWidth = 80)
        .forEach { Timber.d(it) }
}

