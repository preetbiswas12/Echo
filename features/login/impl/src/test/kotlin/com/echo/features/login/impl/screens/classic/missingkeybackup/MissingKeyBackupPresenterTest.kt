/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.classic.missingkeybackup

import com.echo.libraries.core.meta.BuildMeta
import com.echo.libraries.matrix.test.AN_APPLICATION_NAME
import com.echo.libraries.matrix.test.core.aBuildMeta
import com.echo.tests.testutils.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MissingKeyBackupPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.appName).isEqualTo(AN_APPLICATION_NAME)
        }
    }
}

private fun createPresenter(
    buildMeta: BuildMeta = aBuildMeta(applicationName = AN_APPLICATION_NAME),
) = MissingKeyBackupPresenter(
    buildMeta = buildMeta,
)
