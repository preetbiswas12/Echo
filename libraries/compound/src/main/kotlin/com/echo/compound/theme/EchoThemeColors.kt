/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.compound.theme

import androidx.compose.ui.graphics.Color
import com.echo.compound.annotations.CoreColorToken
import com.echo.compound.tokens.generated.SemanticColors
import com.echo.compound.tokens.generated.compoundColorsDark
import com.echo.compound.tokens.generated.compoundColorsLight

/**
 * Echo brand palette.
 */
private val EchoPureBlack = Color(0xFF000000)
private val EchoMidnightNavy = Color(0xFF01042C)
private val EchoDeepNavyBlue = Color(0xFF01164C)
private val EchoRoyalBlue = Color(0xFF012094)
private val EchoElectricBlue = Color(0xFF06399A)
private val EchoCobaltBlue = Color(0xFF164FAD)
private val EchoSoftBlue = Color(0xFF366FC2)
private val EchoIcyBlue = Color(0xFF76A2DF)

/** White, used for text and icons rendered on top of the brand solid colors. */
private val EchoWhite = Color(0xFFFFFFFF)

/**
 * Semantic colors for the dark Echo theme, derived from the Compound dark theme by remapping the
 * accent, action, surface, border, icon and link colors to the Echo brand palette.
 *
 * Critical, success, warning and decorative colors are left untouched.
 */
@OptIn(CoreColorToken::class)
val echoColorsDark: SemanticColors = compoundColorsDark.copy(
    // Accent
    bgAccentHovered = EchoSoftBlue,
    bgAccentPressed = EchoElectricBlue,
    bgAccentRest = EchoCobaltBlue,
    bgAccentSelected = EchoRoyalBlue,
    bgAccentSubtle = EchoRoyalBlue,
    // Actions
    bgActionPrimaryHovered = EchoSoftBlue,
    bgActionPrimaryPressed = EchoElectricBlue,
    bgActionPrimaryRest = EchoCobaltBlue,
    bgActionSecondaryRest = EchoPureBlack,
    bgActionTertiaryHovered = EchoMidnightNavy,
    bgActionTertiaryRest = EchoPureBlack,
    bgActionTertiarySelected = EchoDeepNavyBlue,
    // Badges
    bgBadgeAccent = EchoRoyalBlue,
    bgBadgeDefault = EchoPureBlack,
    bgBadgeInfo = EchoDeepNavyBlue,
    textBadgeAccent = EchoIcyBlue,
    textBadgeInfo = EchoIcyBlue,
    // Canvas and surfaces
    bgCanvasDefault = EchoPureBlack,
    bgCanvasDefaultLevel1 = EchoMidnightNavy,
    bgCanvasDisabled = EchoMidnightNavy,
    bgInfoSubtle = EchoRoyalBlue,
    bgSubtlePrimary = EchoDeepNavyBlue,
    bgSubtleSecondary = EchoMidnightNavy,
    bgSubtleSecondaryLevel0 = EchoPureBlack,
    bgSubtleTertiary = EchoMidnightNavy,
    // Borders
    borderAccentPrimary = EchoCobaltBlue,
    borderAccentSubtle = EchoElectricBlue,
    borderFocused = EchoSoftBlue,
    borderInfoSubtle = EchoElectricBlue,
    // Gradients
    gradientActionStop1 = EchoIcyBlue,
    gradientActionStop2 = EchoSoftBlue,
    gradientActionStop3 = EchoCobaltBlue,
    gradientActionStop4 = EchoRoyalBlue,
    gradientCriticalStop2 = EchoPureBlack,
    gradientInfoStop1 = EchoRoyalBlue,
    gradientInfoStop2 = EchoPureBlack,
    gradientSubtleStop1 = EchoRoyalBlue,
    gradientSubtleStop2 = EchoRoyalBlue,
    gradientSubtleStop3 = EchoDeepNavyBlue,
    gradientSubtleStop4 = EchoDeepNavyBlue,
    gradientSubtleStop5 = EchoMidnightNavy,
    // Icons
    iconAccentPrimary = EchoSoftBlue,
    iconAccentTertiary = EchoCobaltBlue,
    iconInfoPrimary = EchoIcyBlue,
    iconOnSolidPrimary = EchoWhite,
    iconSecondary = EchoIcyBlue,
    iconSecondaryAlpha = EchoIcyBlue.copy(alpha = 0.54f),
    // Separators
    separatorPrimary = EchoDeepNavyBlue,
    separatorSecondary = EchoMidnightNavy,
    // Text
    textActionAccent = EchoSoftBlue,
    textActionPrimary = EchoWhite,
    textInfoPrimary = EchoIcyBlue,
    textLinkExternal = EchoIcyBlue,
    textOnSolidPrimary = EchoWhite,
    textPrimary = EchoWhite,
    textSecondary = EchoIcyBlue,
)

/**
 * Semantic colors for the light Echo theme, derived from the Compound light theme by remapping the
 * accent, action, badge, info, link and icon colors to the Echo brand palette.
 *
 * The light surfaces, neutral text colors and the critical, success, warning and decorative colors
 * are left untouched.
 */
@OptIn(CoreColorToken::class)
val echoColorsLight: SemanticColors = compoundColorsLight.copy(
    // Accent
    bgAccentHovered = EchoElectricBlue,
    bgAccentPressed = EchoRoyalBlue,
    bgAccentRest = EchoCobaltBlue,
    bgAccentSelected = EchoCobaltBlue.copy(alpha = 0.11f),
    bgAccentSubtle = EchoIcyBlue.copy(alpha = 0.15f),
    // Actions
    bgActionPrimaryHovered = EchoSoftBlue,
    bgActionPrimaryPressed = EchoElectricBlue,
    bgActionPrimaryRest = EchoCobaltBlue,
    // Badges
    bgBadgeAccent = EchoCobaltBlue.copy(alpha = 0.20f),
    bgBadgeInfo = EchoIcyBlue.copy(alpha = 0.40f),
    textBadgeAccent = EchoRoyalBlue,
    textBadgeInfo = EchoCobaltBlue,
    // Info
    bgInfoSubtle = EchoIcyBlue.copy(alpha = 0.12f),
    // Borders
    borderFocused = EchoCobaltBlue,
    borderInfoSubtle = EchoIcyBlue,
    // Gradients
    gradientActionStop1 = EchoIcyBlue,
    gradientActionStop2 = EchoSoftBlue,
    gradientActionStop3 = EchoCobaltBlue,
    gradientActionStop4 = EchoRoyalBlue,
    gradientInfoStop1 = EchoIcyBlue.copy(alpha = 0.12f),
    gradientSubtleStop1 = EchoCobaltBlue.copy(alpha = 0.41f),
    gradientSubtleStop2 = EchoCobaltBlue.copy(alpha = 0.23f),
    gradientSubtleStop3 = EchoCobaltBlue.copy(alpha = 0.11f),
    gradientSubtleStop4 = EchoCobaltBlue.copy(alpha = 0.06f),
    gradientSubtleStop5 = EchoCobaltBlue.copy(alpha = 0.03f),
    // Icons and accent text
    iconAccentPrimary = EchoCobaltBlue,
    iconAccentTertiary = EchoSoftBlue,
    iconInfoPrimary = EchoCobaltBlue,
    textActionAccent = EchoCobaltBlue,
    textInfoPrimary = EchoCobaltBlue,
    textLinkExternal = EchoCobaltBlue,
)
