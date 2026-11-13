/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.poll.impl.history

import com.echo.features.poll.impl.history.model.PollHistoryFilter
import com.echo.features.poll.impl.history.model.PollHistoryItem
import com.echo.features.poll.impl.history.model.PollHistoryItems
import kotlinx.collections.immutable.ImmutableList

data class PollHistoryState(
    val isLoading: Boolean,
    val hasMoreToLoad: Boolean,
    val activeFilter: PollHistoryFilter,
    val pollHistoryItems: PollHistoryItems,
    val eventSink: (PollHistoryEvent) -> Unit,
) {
    fun pollHistoryForFilter(filter: PollHistoryFilter): ImmutableList<PollHistoryItem> {
        return when (filter) {
            PollHistoryFilter.ONGOING -> pollHistoryItems.ongoing
            PollHistoryFilter.PAST -> pollHistoryItems.past
        }
    }
}
