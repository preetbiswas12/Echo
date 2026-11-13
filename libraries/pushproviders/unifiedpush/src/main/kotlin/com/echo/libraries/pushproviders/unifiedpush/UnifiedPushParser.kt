/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush

import com.echo.libraries.androidutils.json.JsonProvider
import com.echo.libraries.core.data.tryOrNull
import com.echo.libraries.pushproviders.api.PushData
import dev.zacsweers.metro.Inject

@Inject
class UnifiedPushParser(
    private val json: JsonProvider,
) {
    fun parse(message: ByteArray, clientSecret: String): PushData? {
        return tryOrNull { json().decodeFromString<PushDataUnifiedPush>(String(message)) }?.toPushData(clientSecret)
    }
}
