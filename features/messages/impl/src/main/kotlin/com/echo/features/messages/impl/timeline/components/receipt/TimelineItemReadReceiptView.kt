/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.echo.appconfig.TimelineConfig
import com.echo.compound.theme.ElementTheme
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.messages.impl.timeline.model.ReadReceiptData
import com.echo.libraries.designsystem.components.avatar.Avatar
import com.echo.libraries.designsystem.components.avatar.AvatarData
import com.echo.libraries.designsystem.components.avatar.AvatarSize
import com.echo.libraries.designsystem.components.avatar.AvatarType
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.Icon
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.matrix.api.timeline.item.event.LocalEventSendState
import com.echo.libraries.matrix.ui.messages.ProvideRoomMemberProfiles
import com.echo.libraries.matrix.ui.messages.rememberRoomMember
import com.echo.libraries.previewutils.room.aRoomMemberList
import com.echo.libraries.testtags.TestTags
import com.echo.libraries.ui.strings.CommonPlurals
import com.echo.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun TimelineItemReadReceiptView(
    state: ReadReceiptViewState,
    onReadReceiptsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content: @Composable (() -> Unit)? = when (state.sendState) {
        // If null, assume it's Sent and act the same way
        null if state.isLastOutgoingMessage && state.receipts.isEmpty() -> {
            @Composable { SentIcon() }
        }
        is LocalEventSendState.Sending -> {
            @Composable { SendingIcon() }
        }
        is LocalEventSendState.Failed -> {
            // Error? The timestamp is already displayed in red
            null
        }
        is LocalEventSendState.Sent if state.isLastOutgoingMessage && state.receipts.isEmpty() -> {
            @Composable { SentIcon() }
        }
        else -> if (state.receipts.isNotEmpty()) {
            @Composable {
                ReadReceiptsAvatars(
                    receipts = state.receipts,
                    onClick = onReadReceiptsClick,
                )
            }
        } else {
            null
        }
    }

    // Only show the row if there is content to display
    content?.let {
        ReadReceiptsRow(
            modifier = modifier,
            content = it,
        )
    }
}

@Composable
private fun SendingIcon() {
    Icon(
        modifier = Modifier.padding(2.dp),
        imageVector = CompoundIcons.Circle(),
        contentDescription = stringResource(id = CommonStrings.common_sending),
        tint = ElementTheme.colors.iconSecondary
    )
}

@Composable
private fun SentIcon() {
    Icon(
        modifier = Modifier.padding(2.dp),
        imageVector = CompoundIcons.CheckCircle(),
        contentDescription = stringResource(id = CommonStrings.common_sent),
        tint = ElementTheme.colors.iconSecondary
    )
}

@Composable
private fun ReadReceiptsRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AvatarSize.TimelineReadReceipt.dp + 8.dp)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ReadReceiptsAvatars(
    receipts: ImmutableList<ReadReceiptData>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avatarSize = AvatarSize.TimelineReadReceipt.dp
    val avatarStrokeSize = 1.dp
    val avatarStrokeColor = ElementTheme.colors.bgCanvasDefault
    val receiptDescription = computeReceiptDescription(receipts)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                onClick = onClick,
                onClickLabel = stringResource(id = CommonStrings.a11y_read_receipts_tap_to_show_all_android),
            )
            .padding(2.dp)
            .clearAndSetSemantics {
                testTag = TestTags.messageReadReceipts.value
                contentDescription = receiptDescription
                role = Role.Button
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp - avatarStrokeSize),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.CenterEnd,
        ) {
            receipts
                .take(TimelineConfig.MAX_READ_RECEIPT_TO_DISPLAY)
                .reversed()
                .forEachIndexed { index, readReceiptData ->
                    val member = rememberRoomMember(readReceiptData.userId)
                    Box(
                        modifier = Modifier
                            .padding(end = (12.dp + avatarStrokeSize * 2) * index)
                            .size(size = avatarSize + avatarStrokeSize * 2)
                            .clip(CircleShape)
                            .background(avatarStrokeColor)
                            .zIndex(index.toFloat()),
                        contentAlignment = Alignment.Center,
                    ) {
                        Avatar(
                            avatarData = AvatarData(
                                id = readReceiptData.userId.value,
                                name = member?.displayName,
                                url = member?.avatarUrl,
                                size = AvatarSize.TimelineReadReceipt,
                            ),
                            avatarType = AvatarType.User,
                        )
                    }
                }
        }
        if (receipts.size > TimelineConfig.MAX_READ_RECEIPT_TO_DISPLAY) {
            Text(
                text = "+" + (receipts.size - TimelineConfig.MAX_READ_RECEIPT_TO_DISPLAY),
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun computeReceiptDescription(receipts: ImmutableList<ReadReceiptData>): String {
    return when (receipts.size) {
        0 -> "" // Cannot happen
        1 -> stringResource(
            id = CommonStrings.a11y_read_receipts_single,
            rememberBestName(receipts[0])
        )
        2 -> stringResource(
            id = CommonStrings.a11y_read_receipts_multiple,
            rememberBestName(receipts[0]),
            rememberBestName(receipts[1]),
        )
        else -> pluralStringResource(
            id = CommonPlurals.a11y_read_receipts_multiple_with_others,
            count = receipts.size - 1,
            rememberBestName(receipts[0]),
            receipts.size - 1
        )
    }
}

@Composable
private fun rememberBestName(readReceiptData: ReadReceiptData): String {
    return rememberRoomMember(readReceiptData.userId)?.displayName ?: readReceiptData.userId.value
}

@PreviewsDayNight
@Composable
internal fun TimelineItemReadReceiptViewPreview(
    @PreviewParameter(ReadReceiptViewStatePreviewParam::class) state: ReadReceiptViewState,
) = ElementPreview {
    ProvideRoomMemberProfiles(members = aRoomMemberList()) {
        TimelineItemReadReceiptView(
            state = state,
            onReadReceiptsClick = {},
        )
    }
}
