/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.api

/**
 * Represents a pending task in the SDK that can be canceled and closed.
 */
interface SdkPendingTask : AutoCloseable {
    /** Checks if the task is still running. */
    fun isRunning(): Boolean
}
