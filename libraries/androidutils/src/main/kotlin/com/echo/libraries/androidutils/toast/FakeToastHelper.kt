/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.androidutils.toast

class FakeToastHelper : ToastHelper {
    val shownToasts = mutableListOf<Int>()

    override fun show(resId: Int) {
        shownToasts.add(resId)
    }
}

