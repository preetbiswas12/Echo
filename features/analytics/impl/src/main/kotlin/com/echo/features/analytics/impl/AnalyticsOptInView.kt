/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.analytics.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.appconfig.AnalyticsConfig
import com.echo.compound.theme.ElementTheme
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.analytics.api.AnalyticsOptInEvent
import com.echo.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import com.echo.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import com.echo.libraries.designsystem.atomic.pages.HeaderFooterPage
import com.echo.libraries.designsystem.background.OnboardingBackground
import com.echo.libraries.designsystem.components.BigIcon
import com.echo.libraries.designsystem.components.ClickableLinkText
import com.echo.libraries.designsystem.components.visuallist.VisualList
import com.echo.libraries.designsystem.components.visuallist.VisualListItemData
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import com.echo.libraries.designsystem.theme.components.Button
import com.echo.libraries.designsystem.theme.components.ButtonSize
import com.echo.libraries.designsystem.theme.components.TextButton
import com.echo.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AnalyticsOptInView(
    state: AnalyticsOptInState,
    onClickTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink

    fun onAcceptTerms() {
        eventSink(AnalyticsOptInEvent.EnableAnalytics(true))
    }

    fun onDeclineTerms() {
        eventSink(AnalyticsOptInEvent.EnableAnalytics(false))
    }

    BackHandler(onBack = ::onDeclineTerms)
    HeaderFooterPage(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        background = { OnboardingBackground() },
        header = { AnalyticsOptInHeader(state, onClickTerms) },
        content = { AnalyticsOptInContent() },
        footer = {
            AnalyticsOptInFooter(
                onAcceptTerms = ::onAcceptTerms,
                onDeclineTerms = ::onDeclineTerms,
            )
        }
    )
}

private const val LINK_TAG = "link"

@Composable
private fun AnalyticsOptInHeader(
    state: AnalyticsOptInState,
    onClickTerms: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTitleSubtitleMolecule(
            modifier = Modifier.padding(top = 60.dp, bottom = 28.dp),
            title = stringResource(id = R.string.screen_analytics_prompt_title, state.applicationName),
            subTitle = stringResource(id = R.string.screen_analytics_prompt_help_us_improve),
            iconStyle = BigIcon.Style.Default(CompoundIcons.Chart())
        )
        if (state.hasPolicyLink) {
            val text = buildAnnotatedStringWithStyledPart(
                R.string.screen_analytics_prompt_read_terms,
                R.string.screen_analytics_prompt_read_terms_content_link,
                color = Color.Unspecified,
                underline = false,
                bold = true,
                tagAndLink = LINK_TAG to AnalyticsConfig.POLICY_LINK,
            )
            ClickableLinkText(
                annotatedString = text,
                onClick = { onClickTerms() },
                modifier = Modifier
                    .padding(8.dp),
                style = ElementTheme.typography.fontBodyMdRegular
                    .copy(
                        color = ElementTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
            )
        }
    }
}

@Composable
private fun AnalyticsOptInContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BiasAlignment(
            horizontalBias = 0f,
            verticalBias = -0.4f
        )
    ) {
        VisualList(
            items = persistentListOf(
                VisualListItemData(
                    message = stringResource(id = R.string.screen_analytics_prompt_data_usage),
                    iconVector = CompoundIcons.CheckCircle(),
                ),
                VisualListItemData(
                    message = stringResource(id = R.string.screen_analytics_prompt_third_party_sharing),
                    iconVector = CompoundIcons.CheckCircle(),
                ),
                VisualListItemData(
                    message = stringResource(id = R.string.screen_analytics_prompt_settings),
                    iconVector = CompoundIcons.CheckCircle(),
                ),
            ),
            iconTint = ElementTheme.colors.iconSuccessPrimary,
        )
    }
}

@Composable
private fun AnalyticsOptInFooter(
    onAcceptTerms: () -> Unit,
    onDeclineTerms: () -> Unit,
) {
    ButtonColumnMolecule {
        Button(
            text = stringResource(id = CommonStrings.action_ok),
            onClick = onAcceptTerms,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(
            text = stringResource(id = CommonStrings.action_not_now),
            size = ButtonSize.Medium,
            onClick = onDeclineTerms,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AnalyticsOptInViewPreview(@PreviewParameter(AnalyticsOptInStatePreviewParam::class) state: AnalyticsOptInState) = ElementPreview {
    AnalyticsOptInView(
        state = state,
        onClickTerms = {},
    )
}

