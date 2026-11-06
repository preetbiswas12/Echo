/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.invite.impl.acceptdecline

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.features.invite.api.InviteData
import com.echo.features.invite.api.acceptdecline.AcceptDeclineInviteState
import com.echo.features.invite.api.acceptdecline.ConfirmingDeclineInvite
import com.echo.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import com.echo.features.invite.impl.AcceptInvite
import com.echo.libraries.architecture.AsyncAction
import com.echo.libraries.designsystem.preview.ROOM_NAME
import com.echo.libraries.matrix.api.core.RoomId

open class AcceptDeclineInviteStatePreviewParam : PreviewParameterProvider<AcceptDeclineInviteState> {
    override val values: Sequence<AcceptDeclineInviteState>
        get() = sequenceOf(
            anAcceptDeclineInviteState(),
            anAcceptDeclineInviteState(
                declineAction = ConfirmingDeclineInvite(
                    InviteData(
                        roomId = RoomId("!room:matrix.org"),
                        isDm = true,
                        roomName = ROOM_NAME,
                    ),
                    blockUser = false,
                ),
            ),
            anAcceptDeclineInviteState(
                declineAction = ConfirmingDeclineInvite(
                    InviteData(
                        roomId = RoomId("!room:matrix.org"),
                        isDm = true,
                        roomName = ROOM_NAME,
                    ),
                    blockUser = true,
                ),
            ),
            anAcceptDeclineInviteState(
                acceptAction = AsyncAction.Failure(RuntimeException("Error while accepting invite")),
            ),
            anAcceptDeclineInviteState(
                acceptAction = AsyncAction.Failure(AcceptInvite.Failures.InvalidInvite),
            ),
            anAcceptDeclineInviteState(
                declineAction = AsyncAction.Failure(RuntimeException("Error while declining invite")),
            ),
        )
}

