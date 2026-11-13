/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.accountprovider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.compound.theme.ElementTheme
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.libraries.designsystem.atomic.atoms.RoundedIconAtom
import com.echo.libraries.designsystem.atomic.atoms.RoundedIconAtomSize
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.HorizontalDivider
import com.echo.libraries.designsystem.theme.components.Icon
import com.echo.libraries.designsystem.theme.components.Text
import com.echo.libraries.matrix.api.accountprovider.AccountProvider

/**
 * https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=604-60817
 */
@Composable
fun AccountProviderView(
    item: AccountProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onClick() }
    ) {
        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundedIconAtom(
                    size = RoundedIconAtomSize.Medium,
                    imageVector = CompoundIcons.Host(),
                    tint = ElementTheme.colors.iconPrimary,
                )
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    text = item.friendlyServerName(),
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textPrimary,
                )
                if (selected) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 10.dp),
                        imageVector = CompoundIcons.Check(),
                        contentDescription = null,
                        tint = ElementTheme.colors.iconAccentPrimary,
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AccountProviderViewPreview(@PreviewParameter(AccountProviderPreviewParam::class) item: AccountProvider) = ElementPreview {
    AccountProviderView(
        item = item,
        onClick = { }
    )
}
