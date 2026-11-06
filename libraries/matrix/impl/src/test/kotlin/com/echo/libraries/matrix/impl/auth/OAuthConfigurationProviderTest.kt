/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.impl.auth

import com.google.common.truth.Truth.assertThat
import com.echo.libraries.matrix.test.auth.FAKE_REDIRECT_URL
import com.echo.libraries.matrix.test.auth.FakeOAuthRedirectUrlProvider
import com.echo.libraries.matrix.test.core.aBuildMeta
import org.junit.Test

class OAuthConfigurationProviderTest {
    @Test
    fun get() {
        val result = OAuthConfigurationProvider(
            buildMeta = aBuildMeta(
                applicationName = "myName",
            ),
            oAuthRedirectUrlProvider = FakeOAuthRedirectUrlProvider(),
        ).get()
        assertThat(result.clientName).isEqualTo("myName")
        assertThat(result.redirectUri).isEqualTo(FAKE_REDIRECT_URL)
    }
}

