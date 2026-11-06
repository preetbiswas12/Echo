/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.draft

import dev.zacsweers.metro.ContributesBinding
import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.matrix.api.room.draft.ComposerDraft

@ContributesBinding(RoomScope::class)
class DefaultComposerDraftService(
    private val volatileComposerDraftStore: VolatileComposerDraftStore,
    private val matrixComposerDraftStore: MatrixComposerDraftStore,
) : ComposerDraftService {
    override suspend fun loadDraft(roomId: RoomId, threadRoot: ThreadId?, isVolatile: Boolean): ComposerDraft? {
        return getStore(isVolatile).loadDraft(roomId, threadRoot)
    }

    override suspend fun updateDraft(roomId: RoomId, threadRoot: ThreadId?, draft: ComposerDraft?, isVolatile: Boolean) {
        getStore(isVolatile).updateDraft(roomId, threadRoot, draft)
    }

    private fun getStore(isVolatile: Boolean): ComposerDraftStore {
        return if (isVolatile) {
            volatileComposerDraftStore
        } else {
            matrixComposerDraftStore
        }
    }
}

