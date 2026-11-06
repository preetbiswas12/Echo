/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.viewfolder.impl.folder

import com.echo.features.viewfolder.impl.model.Item
import kotlinx.collections.immutable.ImmutableList

data class ViewFolderState(
    val title: String,
    val content: ImmutableList<Item>,
)

