/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test.media

import com.echo.libraries.matrix.api.media.MediaPreviewConfig
import com.echo.libraries.matrix.api.media.MediaPreviewService
import com.echo.libraries.matrix.api.media.MediaPreviewValue
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeMediaPreviewService(
    override val mediaPreviewConfigFlow: StateFlow<MediaPreviewConfig> = MutableStateFlow(MediaPreviewConfig.DEFAULT),
    private val fetchMediaPreviewConfigResult: () -> Result<MediaPreviewConfig?> = { lambdaError() },
    private val setMediaPreviewValueResult: (MediaPreviewValue) -> Result<Unit> = { lambdaError() },
    private val setHideInviteAvatarsResult: (Boolean) -> Result<Unit> = { lambdaError() },
) : MediaPreviewService {
    override suspend fun fetchMediaPreviewConfig(): Result<MediaPreviewConfig?> = simulateLongTask {
        fetchMediaPreviewConfigResult()
    }

    override suspend fun setMediaPreviewValue(mediaPreviewValue: MediaPreviewValue): Result<Unit> = simulateLongTask {
        setMediaPreviewValueResult(mediaPreviewValue)
    }

    override suspend fun setHideInviteAvatars(hide: Boolean): Result<Unit> = simulateLongTask {
        setHideInviteAvatarsResult(hide)
    }
}

