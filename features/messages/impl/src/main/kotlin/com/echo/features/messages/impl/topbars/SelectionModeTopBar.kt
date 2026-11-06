/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.topbars

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.messages.impl.R
import com.echo.features.messages.impl.timeline.SelectionAction
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.Icon
import com.echo.libraries.designsystem.theme.components.IconButton
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.designsystem.theme.components.TopAppBar
import com.echo.libraries.ui.strings.CommonPlurals
import com.echo.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionModeTopBar(
    selectedCount: Int,
    action: SelectionAction,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val a11ySelectedCount = pluralStringResource(R.plurals.screen_room_selection_count, selectedCount, selectedCount)
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = pluralStringResource(CommonPlurals.common_selected_count, selectedCount, selectedCount),
                modifier = Modifier.semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = a11ySelectedCount
                },
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onCancelClick,
            ) {
                Icon(imageVector = CompoundIcons.Close(), contentDescription = stringResource(CommonStrings.action_close))
            }
        },
        actions = {
            IconButton(
                onClick = onConfirmClick,
                enabled = selectedCount > 0,
            ) {
                Icon(
                    resourceId = action.iconRes,
                    contentDescription = stringResource(action.titleRes),
                )
            }
        },
    )
}

@PreviewsDayNight
@Composable
internal fun SelectionModeTopBarPreview(
    @PreviewParameter(SelectionCountPreviewParam::class) count: Int,
) = ElementPreview {
    SelectionModeTopBar(
        selectedCount = count,
        action = SelectionAction.Forward,
        onCancelClick = {},
        onConfirmClick = {},
    )
}

internal class SelectionCountPreviewParam : PreviewParameterProvider<Int> {
    override val values = sequenceOf(0, 1, 5)
}

