/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.search

import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.search.MessageSearchResult
import com.echo.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import com.echo.libraries.matrix.impl.timeline.item.event.map
import org.matrix.rustcomponents.sdk.SearchServiceResult

class MessageSearchResultMapper(
    private val contentMapper: TimelineEventContentMapper = TimelineEventContentMapper(),
) {
    /**
     * Note: [TimelineEventContentMapper.map] consumes (and destroys) the content, so each result
     * must be mapped exactly once.
     */
    fun map(result: SearchServiceResult): MessageSearchResult {
        // Exhaustive on purpose, with no `else`: a new SDK result kind must fail compilation here
        // rather than being silently dropped, which would desync our list from the SDK's indices.
        return when (result) {
            is SearchServiceResult.Message -> MessageSearchResult(
                roomId = RoomId(result.roomId),
                eventId = EventId(result.result.eventId),
                senderId = UserId(result.result.sender),
                senderProfile = result.result.senderProfile.map(),
                content = contentMapper.map(result.result.content),
                timestamp = result.result.timestamp.toLong(),
            )
        }
    }
}

