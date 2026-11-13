/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.dateformatter.impl.previews

import android.content.Context
import com.echo.libraries.dateformatter.impl.DateFormatterFull
import com.echo.libraries.dateformatter.impl.DateFormatterMonth
import com.echo.libraries.dateformatter.impl.DateFormatterTime
import com.echo.libraries.dateformatter.impl.DateFormatterTimeOnly
import com.echo.libraries.dateformatter.impl.DateFormatters
import com.echo.libraries.dateformatter.impl.DefaultDateFormatter
import com.echo.libraries.dateformatter.impl.DefaultDateFormatterDay
import com.echo.libraries.dateformatter.impl.LocalDateTimeProvider
import kotlinx.datetime.TimeZone
import java.util.Locale
import kotlin.time.Instant

/**
 * Create DefaultDateFormatter and set current time to the provided date.
 */
fun createFormatter(
    context: Context,
    currentDate: String,
    locale: Locale,
): DefaultDateFormatter {
    val clock = PreviewClock().apply { givenInstant(Instant.parse(currentDate)) }
    val localDateTimeProvider = LocalDateTimeProvider(clock) { TimeZone.UTC }
    val dateFormatters = DateFormatters(
        localeChangeObserver = {},
        clock = clock,
        timeZoneProvider = { TimeZone.UTC },
        locale = locale,
    )
    val stringProvider = PreviewStringProvider(context.resources)
    val dateFormatterDay = DefaultDateFormatterDay(
        localDateTimeProvider = localDateTimeProvider,
        dateFormatters = dateFormatters,
    )
    return DefaultDateFormatter(
        dateFormatterFull = DateFormatterFull(
            stringProvider = stringProvider,
            localDateTimeProvider = localDateTimeProvider,
            dateFormatters = dateFormatters,
            dateFormatterDay = dateFormatterDay,
        ),
        dateFormatterMonth = DateFormatterMonth(
            stringProvider = stringProvider,
            localDateTimeProvider = localDateTimeProvider,
            dateFormatters = dateFormatters,
        ),
        dateFormatterDay = dateFormatterDay,
        dateFormatterTime = DateFormatterTime(
            localDateTimeProvider = localDateTimeProvider,
            dateFormatters = dateFormatters,
        ),
        dateFormatterTimeOnly = DateFormatterTimeOnly(
            localDateTimeProvider = localDateTimeProvider,
            dateFormatters = dateFormatters,
        ),
    )
}
