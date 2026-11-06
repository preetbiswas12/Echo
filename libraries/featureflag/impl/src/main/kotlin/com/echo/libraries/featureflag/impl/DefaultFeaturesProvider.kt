/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.featureflag.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import com.echo.libraries.featureflag.api.Feature
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.featureflag.api.FeaturesProvider

@ContributesIntoSet(AppScope::class)
class DefaultFeaturesProvider : FeaturesProvider {
    override fun provide(): List<Feature> = FeatureFlags.entries
}

