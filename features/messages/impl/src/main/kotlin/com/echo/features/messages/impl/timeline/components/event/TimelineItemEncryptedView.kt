/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.echo.features.messages.impl.R
import com.echo.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import com.echo.features.messages.impl.timeline.model.event.TimelineItemEncryptedContentPreviewParam
import com.echo.libraries.designsystem.icons.CompoundDrawables
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import com.echo.libraries.matrix.api.timeline.item.event.UtdCause
import com.echo.libraries.ui.common.layout.ContentAvoidingLayoutData
import com.echo.libraries.ui.strings.CommonStrings

@Composable
fun TimelineItemEncryptedView(
    content: TimelineItemEncryptedContent,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier
) {
    val (textId, iconId) = when (content.data) {
        is UnableToDecryptContent.Data.MegolmV1AesSha2 -> {
            when (content.data.utdCause) {
                UtdCause.SentBeforeWeJoined -> {
                    R.string.common_unable_to_decrypt_no_access to CompoundDrawables.ic_compound_block
                }
                UtdCause.VerificationViolation -> {
                    R.string.common_unable_to_decrypt_verification_violation to CompoundDrawables.ic_compound_block
                }
                UtdCause.UnsignedDevice,
                UtdCause.UnknownDevice -> {
                    R.string.common_unable_to_decrypt_insecure_device to CompoundDrawables.ic_compound_block
                }
                UtdCause.HistoricalMessageAndBackupIsDisabled -> {
                    R.string.timeline_decryption_failure_historical_event_no_key_backup to CompoundDrawables.ic_compound_block
                }
                UtdCause.HistoricalMessageAndDeviceIsUnverified -> {
                    R.string.timeline_decryption_failure_historical_event_unverified_device to CompoundDrawables.ic_compound_block
                }
                UtdCause.WithheldUnverifiedOrInsecureDevice -> {
                    R.string.timeline_decryption_failure_withheld_unverified to CompoundDrawables.ic_compound_block
                }
                UtdCause.WithheldBySender -> {
                    R.string.timeline_decryption_failure_unable_to_decrypt to CompoundDrawables.ic_compound_error
                }
                else -> {
                    CommonStrings.common_waiting_for_decryption_key to CompoundDrawables.ic_compound_time
                }
            }
        }
        else -> {
            // Should not happen, we only supports megolm in rooms
            CommonStrings.common_waiting_for_decryption_key to CompoundDrawables.ic_compound_time
        }
    }
    TimelineItemInformativeView(
        text = stringResource(id = textId),
        iconDescription = stringResource(id = CommonStrings.dialog_title_warning),
        iconResourceId = iconId,
        onContentLayoutChange = onContentLayoutChange,
        modifier = modifier
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEncryptedViewPreview(
    @PreviewParameter(TimelineItemEncryptedContentPreviewParam::class) content: TimelineItemEncryptedContent
) = ElementPreview {
    TimelineItemEncryptedView(
        content = content,
        onContentLayoutChange = {},
    )
}

