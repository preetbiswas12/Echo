/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.mediaviewer.impl.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.echo.annotations.ContributesNode
import com.echo.libraries.architecture.callback
import com.echo.libraries.di.RoomScope
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.mediaviewer.impl.gallery.di.LocalMediaItemPresenterFactories
import com.echo.libraries.mediaviewer.impl.gallery.di.MediaItemPresenterFactories
import com.echo.libraries.mediaviewer.impl.model.MediaItem
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@ContributesNode(RoomScope::class)
@AssistedInject
class MediaGalleryNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: MediaGalleryPresenter.Factory,
    private val mediaItemPresenterFactories: MediaItemPresenterFactories,
) : Node(buildContext, plugins = plugins),
    MediaGalleryNavigator {
    private val presenter = presenterFactory.create(
        navigator = this,
    )

    interface Callback : Plugin {
        fun onBackClick()
        fun showItem(item: MediaItem.Event)
        fun viewInTimeline(eventId: EventId)
        fun forward(eventId: EventId)
    }

    private val callback: Callback = callback()

    override fun onViewInTimelineClick(eventId: EventId) {
        callback.viewInTimeline(eventId)
    }

    override fun onForwardClick(eventId: EventId) {
        callback.forward(eventId)
    }

    @Composable
    override fun View(modifier: Modifier) {
        CompositionLocalProvider(
            LocalMediaItemPresenterFactories provides mediaItemPresenterFactories,
        ) {
            val state = presenter.present()
            MediaGalleryView(
                state = state,
                onBackClick = callback::onBackClick,
                onItemClick = callback::showItem,
                modifier = modifier,
            )
        }
    }
}
