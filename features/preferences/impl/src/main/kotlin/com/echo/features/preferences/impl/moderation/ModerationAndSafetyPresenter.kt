/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.moderation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.di.annotations.SessionCoroutineScope
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.preferences.api.store.SessionPreferencesStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Inject
class ModerationAndSafetyPresenter(
    private val matrixClient: MatrixClient,
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val mediaPreviewConfigStateStore: MediaPreviewConfigStateStore,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<ModerationAndSafetyState> {
    @Composable
    override fun present(): ModerationAndSafetyState {
        val isSharePresenceEnabled by remember {
            sessionPreferencesStore.isSharePresenceEnabled()
        }.collectAsState(initial = true)

        val mediaPreviewConfigState = mediaPreviewConfigStateStore.state()

        val numberOfBlockedUsers by produceState(initialValue = 0) {
            matrixClient.ignoredUsersFlow
                .onEach { value = it.size }
                .launchIn(this)
        }

        fun handleEvent(event: ModerationAndSafetyEvent) {
            when (event) {
                is ModerationAndSafetyEvent.SetSharePresenceEnabled -> sessionCoroutineScope.launch {
                    sessionPreferencesStore.setSharePresence(event.enabled)
                }
                is ModerationAndSafetyEvent.SetHideInviteAvatars -> mediaPreviewConfigStateStore.setHideInviteAvatars(event.value)
                is ModerationAndSafetyEvent.SetTimelineMediaPreviewValue -> mediaPreviewConfigStateStore.setTimelineMediaPreviewValue(event.value)
            }
        }

        return ModerationAndSafetyState(
            isSharePresenceEnabled = isSharePresenceEnabled,
            mediaPreviewConfigState = mediaPreviewConfigState,
            numberOfBlockedUsers = numberOfBlockedUsers,
            eventSink = ::handleEvent,
        )
    }
}
