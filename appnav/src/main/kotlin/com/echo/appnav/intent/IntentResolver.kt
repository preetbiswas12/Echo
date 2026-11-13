/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.appnav.intent

import android.content.Intent
import com.echo.features.login.api.LoginIntentResolver
import com.echo.features.login.api.LoginParams
import com.echo.features.share.api.ShareIntentData
import com.echo.features.share.api.ShareIntentHandler
import com.echo.libraries.deeplink.api.DeeplinkData
import com.echo.libraries.deeplink.api.DeeplinkParser
import com.echo.libraries.matrix.api.permalink.PermalinkData
import com.echo.libraries.matrix.api.permalink.PermalinkParser
import com.echo.libraries.oauth.api.OAuthAction
import com.echo.libraries.oauth.api.OAuthIntentResolver
import dev.zacsweers.metro.Inject
import timber.log.Timber

sealed interface ResolvedIntent {
    data class Navigation(val deeplinkData: DeeplinkData) : ResolvedIntent
    data class OAuth(val oAuthAction: OAuthAction) : ResolvedIntent
    data class Permalink(val permalinkData: PermalinkData) : ResolvedIntent
    data class Login(val params: LoginParams) : ResolvedIntent
    data class IncomingShare(val shareIntentData: ShareIntentData) : ResolvedIntent
}

@Inject
class IntentResolver(
    private val deeplinkParser: DeeplinkParser,
    private val loginIntentResolver: LoginIntentResolver,
    private val oAuthIntentResolver: OAuthIntentResolver,
    private val permalinkParser: PermalinkParser,
    private val shareIntentHandler: ShareIntentHandler,
) {
    fun resolve(intent: Intent): ResolvedIntent? {
        if (intent.canBeIgnored()) return null

        // Coming from a notification?
        val deepLinkData = deeplinkParser.getFromIntent(intent)
        if (deepLinkData != null) return ResolvedIntent.Navigation(deepLinkData)

        // Coming during login using OAuth?
        val oAuthAction = oAuthIntentResolver.resolve(intent)
        if (oAuthAction != null) return ResolvedIntent.OAuth(oAuthAction)

        val actionViewData = intent
            .takeIf { it.action == Intent.ACTION_VIEW }
            ?.dataString

        // Mobile configuration link clicked? (mobile.element.io)
        val mobileLoginData = actionViewData
            ?.let { loginIntentResolver.parse(it) }
        if (mobileLoginData != null) return ResolvedIntent.Login(mobileLoginData)

        // External link clicked? (matrix.to, element.io, etc.)
        val permalinkData = actionViewData
            ?.let { permalinkParser.parse(it) }
            ?.takeIf { it !is PermalinkData.FallbackLink }
        if (permalinkData != null) return ResolvedIntent.Permalink(permalinkData)

        if (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) {
            val data = shareIntentHandler.handleIncomingShareIntent(intent) ?: return null
            return ResolvedIntent.IncomingShare(data)
        }

        // Unknown intent
        Timber.w("Unknown intent")
        return null
    }
}

private fun Intent.canBeIgnored(): Boolean {
    if (flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return true
    return action == Intent.ACTION_MAIN &&
        categories?.contains(Intent.CATEGORY_LAUNCHER) == true
}
