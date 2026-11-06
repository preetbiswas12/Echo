/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaplayer.impl.di

import android.content.Context
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import com.echo.libraries.di.RoomScope
import com.echo.libraries.di.annotations.ApplicationContext
import com.echo.libraries.mediaplayer.impl.DefaultSimplePlayer
import com.echo.libraries.mediaplayer.impl.SimplePlayer

@ContributesTo(RoomScope::class)
@BindingContainer
object SimplePlayerBindingContainer {
    @Provides
    fun providesSimplePlayer(
        @ApplicationContext context: Context,
    ): SimplePlayer = DefaultSimplePlayer(ExoPlayer.Builder(context).setWakeMode(C.WAKE_MODE_LOCAL).build())
}

