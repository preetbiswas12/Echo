/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.call.impl.utils

import com.echo.features.call.impl.data.WidgetMessage
import com.echo.libraries.androidutils.json.JsonProvider
import com.echo.libraries.core.extensions.runCatchingExceptions
import dev.zacsweers.metro.Inject

@Inject
class WidgetMessageSerializer(
    private val json: JsonProvider,
) {
    fun deserialize(message: String): Result<WidgetMessage> {
        return runCatchingExceptions { json().decodeFromString(WidgetMessage.serializer(), message) }
    }

    fun serialize(message: WidgetMessage): String {
        return json().encodeToString(WidgetMessage.serializer(), message)
    }
}
