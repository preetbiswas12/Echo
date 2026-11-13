/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.moderation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.preferences.impl.R
import com.echo.libraries.architecture.coverage.ExcludeFromCoverage
import com.echo.libraries.designsystem.components.list.ListItemContent
import com.echo.libraries.designsystem.components.preferences.PreferenceCategory
import com.echo.libraries.designsystem.components.preferences.PreferencePage
import com.echo.libraries.designsystem.components.preferences.PreferenceSwitch
import com.echo.libraries.designsystem.preview.ElementPreviewBlack
import com.echo.libraries.designsystem.preview.ElementPreviewDark
import com.echo.libraries.designsystem.preview.ElementPreviewLight
import com.echo.libraries.designsystem.preview.PreviewWithLargeHeight
import com.echo.libraries.designsystem.theme.components.IconSource
import com.echo.libraries.designsystem.theme.components.ListItem
import com.echo.libraries.designsystem.theme.components.ListSectionHeader
import com.echo.libraries.designsystem.theme.components.ListSupportingText
import com.echo.libraries.designsystem.theme.components.ListSupportingTextDefaults
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.designsystem.utils.snackbar.LocalSnackbarDispatcher
import com.echo.libraries.designsystem.utils.snackbar.SnackbarHost
import com.echo.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import com.echo.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import com.echo.libraries.matrix.api.media.MediaPreviewValue
import com.echo.libraries.ui.strings.CommonStrings

/**
 * Ref: https://www.figma.com/design/kEAcfun9iSpszeUDvdKZ6b/ER-351--Multi-account-in-EX?node-id=606-56426
 */
@Composable
fun ModerationAndSafetyView(
    state: ModerationAndSafetyState,
    onBackClick: () -> Unit,
    onOpenBlockedUsers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarDispatcher = LocalSnackbarDispatcher.current
    val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = snackbarMessage)

    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = CommonStrings.common_moderation_and_safety),
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) {
        PresenceSection(state)
        InvitesSection(state)
        MediaSection(state)
        if (state.showBlockedUsersItem) {
            BlockedUsersSection(
                numberOfBlockedUsers = state.numberOfBlockedUsers,
                onOpenBlockedUsers = onOpenBlockedUsers,
            )
        }
    }
}

@Composable
private fun BlockedUsersSection(
    numberOfBlockedUsers: Int,
    onOpenBlockedUsers: () -> Unit,
) {
    PreferenceCategory(
        title = stringResource(R.string.screen_moderation_and_safety_other_users_heading),
    ) {
        ListItem(
            content = { Text(stringResource(id = CommonStrings.common_blocked_users)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Block())),
            onClick = onOpenBlockedUsers,
            trailingContent = ListItemContent.Text(numberOfBlockedUsers.toString()),
        )
    }
}

@Composable
private fun PresenceSection(state: ModerationAndSafetyState) {
    PreferenceCategory(
        title = stringResource(R.string.screen_moderation_and_safety_share_presence_heading),
    ) {
        ListItem(
            content = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_share_presence))
            },
            supportingContent = {
                Text(text = stringResource(id = R.string.screen_advanced_settings_share_presence_description))
            },
            trailingContent = ListItemContent.Switch(
                checked = state.isSharePresenceEnabled,
            ),
            onClick = { state.eventSink(ModerationAndSafetyEvent.SetSharePresenceEnabled(!state.isSharePresenceEnabled)) }
        )
    }
}

@Composable
private fun InvitesSection(
    state: ModerationAndSafetyState,
    modifier: Modifier = Modifier,
) {
    PreferenceCategory(
        modifier = modifier,
        title = stringResource(R.string.screen_moderation_and_safety_invites_heading),
    ) {
        PreferenceSwitch(
            title = stringResource(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title),
            isChecked = state.mediaPreviewConfigState.hideInviteAvatars,
            onCheckedChange = {
                state.eventSink(ModerationAndSafetyEvent.SetHideInviteAvatars(it))
            },
            enabled = !state.mediaPreviewConfigState.setHideInviteAvatarsAction.isLoading()
        )
    }
}

@Composable
private fun ColumnScope.MediaSection(
    state: ModerationAndSafetyState,
) {
    ListSectionHeader(
        title = stringResource(R.string.screen_advanced_settings_show_media_timeline_title),
        hasDivider = true,
        description = {
            ListSupportingText(
                text = stringResource(R.string.screen_advanced_settings_show_media_timeline_subtitle),
                contentPadding = ListSupportingTextDefaults.Padding.None,
            )
        }
    )
    ListItem(
        content = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_always_show)) },
        trailingContent = ListItemContent.RadioButton(
            selected = state.mediaPreviewConfigState.timelineMediaPreviewValue == MediaPreviewValue.On,
            compact = true
        ),
        onClick = {
            state.eventSink(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.On))
        },
        enabled = !state.mediaPreviewConfigState.setTimelineMediaPreviewAction.isLoading()
    )
    ListItem(
        content = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_private_rooms)) },
        trailingContent = ListItemContent.RadioButton(
            selected = state.mediaPreviewConfigState.timelineMediaPreviewValue == MediaPreviewValue.Private,
            compact = true
        ),
        onClick = {
            state.eventSink(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
        },
        enabled = !state.mediaPreviewConfigState.setTimelineMediaPreviewAction.isLoading()
    )
    ListItem(
        content = { Text(text = stringResource(R.string.screen_advanced_settings_show_media_timeline_always_hide)) },
        trailingContent = ListItemContent.RadioButton(
            selected = state.mediaPreviewConfigState.timelineMediaPreviewValue == MediaPreviewValue.Off,
            compact = true
        ),
        onClick = {
            state.eventSink(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
        },
        enabled = !state.mediaPreviewConfigState.setTimelineMediaPreviewAction.isLoading()
    )
}

@PreviewWithLargeHeight
@Composable
internal fun ModerationAndSafetyViewLightPreview(@PreviewParameter(ModerationAndSafetyStatePreviewParam::class) state: ModerationAndSafetyState) =
    ElementPreviewLight { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun ModerationAndSafetyViewDarkPreview(@PreviewParameter(ModerationAndSafetyStatePreviewParam::class) state: ModerationAndSafetyState) =
    ElementPreviewDark { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun ModerationAndSafetyViewBlackPreview(@PreviewParameter(ModerationAndSafetyStatePreviewParam::class) state: ModerationAndSafetyState) =
    ElementPreviewBlack { ContentToPreview(state) }

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(state: ModerationAndSafetyState) {
    ModerationAndSafetyView(
        state = state,
        onBackClick = { },
        onOpenBlockedUsers = { },
    )
}
