/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.draft

import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.matrix.api.room.draft.ComposerDraft

class FakeComposerDraftService : ComposerDraftService {
    var loadDraftLambda: (RoomId, ThreadId?, Boolean) -> ComposerDraft? = { _, _, _ -> null }
    override suspend fun loadDraft(
        roomId: RoomId,
        threadRoot: ThreadId?,
        isVolatile: Boolean
    ): ComposerDraft? = loadDraftLambda(roomId, threadRoot, isVolatile)

    var saveDraftLambda: (RoomId, ThreadId?, ComposerDraft?, Boolean) -> Unit = { _, _, _, _ -> }
    override suspend fun updateDraft(
        roomId: RoomId,
        threadRoot: ThreadId?,
        draft: ComposerDraft?,
        isVolatile: Boolean
    ) = saveDraftLambda(roomId, threadRoot, draft, isVolatile)
}

