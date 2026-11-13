/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrixmedia.test

import coil3.ImageLoader
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrixmedia.api.ImageLoaderHolder

class FakeImageLoaderHolder(
    val fakeImageLoader: ImageLoader = FakeImageLoader(),
) : ImageLoaderHolder {
    override fun get(): ImageLoader {
        return fakeImageLoader
    }

    override fun get(client: MatrixClient): ImageLoader {
        return fakeImageLoader
    }

    override fun remove(sessionId: SessionId) {
        // No-op
    }
}
