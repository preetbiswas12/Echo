/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.enterprise.impl

import com.echo.features.enterprise.api.IsEnterpriseBuild
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class DefaultIsEnterpriseBuild : IsEnterpriseBuild {
    override fun invoke(): Boolean = false
}
