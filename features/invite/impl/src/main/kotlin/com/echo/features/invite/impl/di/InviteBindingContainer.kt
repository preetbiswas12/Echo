/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invite.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import com.echo.features.invite.api.SeenInvitesStore
import com.echo.features.invite.api.acceptdecline.AcceptDeclineInviteState
import com.echo.features.invite.impl.SeenInvitesStoreFactory
import com.echo.features.invite.impl.acceptdecline.AcceptDeclineInvitePresenter
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.SessionScope
import com.echo.libraries.matrix.api.MatrixClient

@ContributesTo(SessionScope::class)
@BindingContainer
interface InviteBindingContainer {
    @Binds
    fun bindAcceptDeclineInvitePresenter(presenter: AcceptDeclineInvitePresenter): Presenter<AcceptDeclineInviteState>

    companion object {
        @Provides
        fun providesSeenInvitesStore(
            factory: SeenInvitesStoreFactory,
            matrixClient: MatrixClient,
        ): SeenInvitesStore {
            return factory.getOrCreate(
                matrixClient.sessionId,
                matrixClient.sessionCoroutineScope,
            )
        }
    }
}

