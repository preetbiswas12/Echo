/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.compound.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.echo.compound.R

/**
 * Echo brand font families: Space Grotesk for headings and branding, Manrope for body and UI text.
 */
val EchoSpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_400, FontWeight.Normal),
    Font(R.font.space_grotesk_500, FontWeight.Medium),
    Font(R.font.space_grotesk_600, FontWeight.SemiBold),
    Font(R.font.space_grotesk_700, FontWeight.Bold),
)

val EchoManrope = FontFamily(
    Font(R.font.manrope_400, FontWeight.Normal),
    Font(R.font.manrope_500, FontWeight.Medium),
    Font(R.font.manrope_600, FontWeight.SemiBold),
    Font(R.font.manrope_700, FontWeight.Bold),
)

/**
 * Echo typography tokens, mirroring `com.echo.compound.tokens.generated.TypographyTokens` (same
 * member names and metrics) with the brand font families and weights applied:
 * - Headings: Space Grotesk. Bold headings use 600 (700 for the branding-sized XL), medium 500.
 * - Body/UI: Manrope. Regular 400, buttons and usernames 600, small labels 500.
 */
object EchoTypographyTokens {
    val fontBodyLgMedium = TextStyle(
        fontFamily = EchoManrope,
        fontWeight = FontWeight.W600,
        lineHeight = 22.sp,
        fontSize = 16.sp,
        letterSpacing = 0.015625.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontBodyLgRegular = TextStyle(
        fontFamily = EchoManrope,
        fontWeight = FontWeight.W400,
        lineHeight = 22.sp,
        fontSize = 16.sp,
        letterSpacing = 0.015625.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontBodyMdMedium = TextStyle(
        fontFamily = EchoManrope,
        fontWeight = FontWeight.W600,
        lineHeight = 20.sp,
        fontSize = 14.sp,
        letterSpacing = 0.017857.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontBodyMdRegular = TextStyle(
        fontFamily = EchoManrope,
        fontWeight = FontWeight.W400,
        lineHeight = 20.sp,
        fontSize = 14.sp,
        letterSpacing = 0.017857.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontBodySmMedium = TextStyle(
        fontFamily = EchoManrope,
        fontWeight = FontWeight.W500,
        lineHeight = 17.sp,
        fontSize = 12.sp,
        letterSpacing = 0.033333.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontBodySmRegular = TextStyle(
        fontFamily = EchoManrope,
        fontWeight = FontWeight.W400,
        lineHeight = 17.sp,
        fontSize = 12.sp,
        letterSpacing = 0.033333.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontBodyXsMedium = TextStyle(
        fontFamily = EchoManrope,
        fontWeight = FontWeight.W500,
        lineHeight = 15.sp,
        fontSize = 11.sp,
        letterSpacing = 0.045454.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontBodyXsRegular = TextStyle(
        fontFamily = EchoManrope,
        fontWeight = FontWeight.W400,
        lineHeight = 15.sp,
        fontSize = 11.sp,
        letterSpacing = 0.045454.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontHeadingLgBold = TextStyle(
        fontFamily = EchoSpaceGrotesk,
        fontWeight = FontWeight.W600,
        lineHeight = 34.sp,
        fontSize = 28.sp,
        letterSpacing = 0.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontHeadingLgRegular = TextStyle(
        fontFamily = EchoSpaceGrotesk,
        fontWeight = FontWeight.W400,
        lineHeight = 34.sp,
        fontSize = 28.sp,
        letterSpacing = 0.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontHeadingMdBold = TextStyle(
        fontFamily = EchoSpaceGrotesk,
        fontWeight = FontWeight.W600,
        lineHeight = 27.sp,
        fontSize = 22.sp,
        letterSpacing = 0.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontHeadingMdRegular = TextStyle(
        fontFamily = EchoSpaceGrotesk,
        fontWeight = FontWeight.W400,
        lineHeight = 27.sp,
        fontSize = 22.sp,
        letterSpacing = 0.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontHeadingSmMedium = TextStyle(
        fontFamily = EchoSpaceGrotesk,
        fontWeight = FontWeight.W500,
        lineHeight = 25.sp,
        fontSize = 20.sp,
        letterSpacing = 0.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontHeadingSmRegular = TextStyle(
        fontFamily = EchoSpaceGrotesk,
        fontWeight = FontWeight.W400,
        lineHeight = 25.sp,
        fontSize = 20.sp,
        letterSpacing = 0.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontHeadingXlBold = TextStyle(
        fontFamily = EchoSpaceGrotesk,
        fontWeight = FontWeight.W700,
        lineHeight = 41.sp,
        fontSize = 34.sp,
        letterSpacing = 0.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val fontHeadingXlRegular = TextStyle(
        fontFamily = EchoSpaceGrotesk,
        fontWeight = FontWeight.W400,
        lineHeight = 41.sp,
        fontSize = 34.sp,
        letterSpacing = 0.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
}

// 32px (Material) vs 34px, it's the closest one
private val echoHeadingXlRegular = EchoTypographyTokens.fontHeadingXlRegular

// both are 28px
private val echoHeadingLgRegular = EchoTypographyTokens.fontHeadingLgRegular

// These are the default M3 values, but we're setting them manually so an update in M3 doesn't break our designs
private val echoHeadlineSmall = TextStyle(
    fontFamily = EchoManrope,
    fontWeight = FontWeight.Normal,
    lineHeight = 32.sp,
    fontSize = 24.sp,
    letterSpacing = 0.em,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
)

// 22px (Material) vs 20px, it's the closest one
private val echoHeadingMdRegular = EchoTypographyTokens.fontHeadingMdRegular

// 16px both
private val echoBodyLgMedium = EchoTypographyTokens.fontBodyLgMedium

// 14px both
private val echoBodyMdMedium = EchoTypographyTokens.fontBodyMdMedium

// 16px both
private val echoBodyLgRegular = EchoTypographyTokens.fontBodyLgRegular

// 14px both
private val echoBodyMdRegular = EchoTypographyTokens.fontBodyMdRegular

// 12px both
private val echoBodySmRegular = EchoTypographyTokens.fontBodySmRegular

// 12px both
private val echoBodySmMedium = EchoTypographyTokens.fontBodySmMedium

// 11px both
private val echoBodyXsMedium = EchoTypographyTokens.fontBodyXsMedium

/**
 * Material 3 [Typography] for the Echo brand, mirroring the Compound token mapping.
 */
val echoTypography = Typography(
    // displayLarge = , 57px (Material) size. We have no equivalent
    // displayMedium = , 45px (Material) size. We have no equivalent
    // displaySmall = , 36px (Material) size. We have no equivalent
    headlineLarge = echoHeadingXlRegular,
    headlineMedium = echoHeadingLgRegular,
    headlineSmall = echoHeadlineSmall,
    titleLarge = echoHeadingMdRegular,
    titleMedium = echoBodyLgMedium,
    titleSmall = echoBodyMdMedium,
    bodyLarge = echoBodyLgRegular,
    bodyMedium = echoBodyMdRegular,
    bodySmall = echoBodySmRegular,
    labelLarge = echoBodyMdMedium,
    labelMedium = echoBodySmMedium,
    labelSmall = echoBodyXsMedium,
)
