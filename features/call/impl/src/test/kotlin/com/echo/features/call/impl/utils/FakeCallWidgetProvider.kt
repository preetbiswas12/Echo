/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.call.impl.utils

import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.test.widget.FakeMatrixWidgetDriver

class FakeCallWidgetProvider(
    private val widgetDriver: FakeMatrixWidgetDriver = FakeMatrixWidgetDriver(),
    private val url: String = "https://call.element.io",
) : CallWidgetProvider {
    var getWidgetCalled = false
        private set

    override suspend fun getWidget(
        sessionId: SessionId,
        roomId: RoomId,
        isAudioCall: Boolean,
        clientId: String,
        languageTag: String?,
        theme: String?
    ): Result<CallWidgetProvider.GetWidgetResult> {
        getWidgetCalled = true
        return Result.success(
            CallWidgetProvider.GetWidgetResult(
                driver = widgetDriver,
                url = url,
            )
        )
    }
}

