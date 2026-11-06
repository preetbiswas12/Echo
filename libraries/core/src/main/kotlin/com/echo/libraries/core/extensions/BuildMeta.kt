/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.core.extensions

import com.echo.libraries.core.meta.BuildMeta
import com.echo.libraries.core.meta.BuildType

fun BuildMeta.isElement(): Boolean {
    return when (buildType) {
        BuildType.RELEASE -> applicationId == "com.echo.x"
        BuildType.NIGHTLY -> applicationId == "com.echo.x.nightly"
        BuildType.DEBUG -> applicationId == "com.echo.x.debug"
    }
}

