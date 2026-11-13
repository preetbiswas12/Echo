/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.home.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.echo.features.home.impl.R
import com.echo.libraries.designsystem.components.Announcement
import com.echo.libraries.designsystem.components.AnnouncementType
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.ui.strings.CommonStrings

@Composable
internal fun SwipeAvatarBanner(
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Announcement(
        modifier = modifier.roomListBannerPadding(),
        title = stringResource(R.string.banner_swipe_avatar_title),
        description = stringResource(R.string.banner_swipe_avatar_content),
        type = AnnouncementType.Actionable(
            actionText = stringResource(CommonStrings.action_ok),
            onActionClick = onDismissClick,
            onDismissClick = onDismissClick,
        ),
    )
}

@PreviewsDayNight
@Composable
internal fun SwipeAvatarBannerPreview() = ElementPreview {
    SwipeAvatarBanner(
        onDismissClick = {},
    )
}
