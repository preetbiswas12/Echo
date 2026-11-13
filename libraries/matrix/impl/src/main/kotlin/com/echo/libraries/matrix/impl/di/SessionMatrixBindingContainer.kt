/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.di

import com.echo.libraries.di.SessionScope
import com.echo.libraries.di.annotations.SessionCoroutineScope
import com.echo.libraries.matrix.api.ClientUrlContentFetcher
import com.echo.libraries.matrix.api.HomeserverCapabilitiesProvider
import com.echo.libraries.matrix.api.MatrixClient
import com.echo.libraries.matrix.api.core.DeviceId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.encryption.EncryptionService
import com.echo.libraries.matrix.api.media.MatrixMediaLoader
import com.echo.libraries.matrix.api.media.MediaPreviewService
import com.echo.libraries.matrix.api.notificationsettings.NotificationSettingsService
import com.echo.libraries.matrix.api.room.RoomMembershipObserver
import com.echo.libraries.matrix.api.roomdirectory.RoomDirectoryService
import com.echo.libraries.matrix.api.roomlist.RoomListService
import com.echo.libraries.matrix.api.scanner.ContentScanner
import com.echo.libraries.matrix.api.search.MessageSearchService
import com.echo.libraries.matrix.api.spaces.SpaceService
import com.echo.libraries.matrix.api.sync.SyncService
import com.echo.libraries.matrix.api.verification.SessionVerificationService
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(SessionScope::class)
object SessionMatrixBindingContainer {
    @Provides
    fun providesSessionId(matrixClient: MatrixClient): SessionId {
        return matrixClient.sessionId
    }

    @Provides
    fun providesDeviceId(matrixClient: MatrixClient): DeviceId {
        return matrixClient.deviceId
    }

    @Provides
    fun providesSessionVerificationService(matrixClient: MatrixClient): SessionVerificationService {
        return matrixClient.sessionVerificationService
    }

    @Provides
    fun providesNotificationSettingsService(matrixClient: MatrixClient): NotificationSettingsService {
        return matrixClient.notificationSettingsService
    }

    @Provides
    fun providesRoomMembershipObserver(matrixClient: MatrixClient): RoomMembershipObserver {
        return matrixClient.roomMembershipObserver
    }

    @Provides
    fun providesRoomListService(matrixClient: MatrixClient): RoomListService {
        return matrixClient.roomListService
    }

    @Provides
    fun providesSyncService(matrixClient: MatrixClient): SyncService {
        return matrixClient.syncService
    }

    @Provides
    fun providesEncryptionService(matrixClient: MatrixClient): EncryptionService {
        return matrixClient.encryptionService
    }

    @Provides
    fun providesMatrixMediaLoader(matrixClient: MatrixClient): MatrixMediaLoader {
        return matrixClient.matrixMediaLoader
    }

    @SessionCoroutineScope
    @Provides
    fun providesSessionCoroutineScope(matrixClient: MatrixClient): CoroutineScope {
        return matrixClient.sessionCoroutineScope
    }

    @Provides
    fun providesRoomDirectoryService(matrixClient: MatrixClient): RoomDirectoryService {
        return matrixClient.roomDirectoryService
    }

    @Provides
    fun providesMediaPreviewService(matrixClient: MatrixClient): MediaPreviewService {
        return matrixClient.mediaPreviewService
    }

    @Provides
    fun providesSpaceService(matrixClient: MatrixClient): SpaceService {
        return matrixClient.spaceService
    }

    @Provides
    fun providesHomeserverCapabilitiesProvider(matrixClient: MatrixClient): HomeserverCapabilitiesProvider {
        return matrixClient.homeserverCapabilities()
    }

    @Provides
    fun providesContentScanner(matrixClient: MatrixClient): ContentScanner? {
        return matrixClient.contentScanner
    }

    @Provides
    fun providesClientUrlContentFetcher(matrixClient: MatrixClient): ClientUrlContentFetcher {
        return matrixClient
    }

    @Provides
    fun providesMessageSearchService(matrixClient: MatrixClient): MessageSearchService {
        return matrixClient.messageSearchService
    }
}
