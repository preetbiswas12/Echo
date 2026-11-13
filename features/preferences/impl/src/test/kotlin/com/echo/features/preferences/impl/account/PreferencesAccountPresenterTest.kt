/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.preferences.impl.account

import com.echo.features.enterprise.api.SessionEnterpriseService
import com.echo.features.enterprise.test.FakeSessionEnterpriseService
import com.echo.features.rageshake.api.RageshakeFeatureAvailability
import com.echo.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.featureflag.test.FakeFeatureFlagService
import com.echo.libraries.indicator.api.IndicatorService
import com.echo.libraries.indicator.test.FakeIndicatorService
import com.echo.libraries.matrix.api.oauth.AccountManagementAction
import com.echo.libraries.matrix.api.user.MatrixUser
import com.echo.libraries.matrix.test.AN_AVATAR_URL
import com.echo.libraries.matrix.test.A_SESSION_ID
import com.echo.libraries.matrix.test.A_USER_NAME
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.verification.FakeSessionVerificationService
import com.echo.tests.testutils.WarmUpRule
import com.echo.tests.testutils.consumeItemsUntilPredicate
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import com.echo.tests.testutils.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PreferencesAccountPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val accountManagementUrlResult = lambdaRecorder<AccountManagementAction?, Result<String?>> { action ->
            Result.success("$action url")
        }
        val matrixClient = FakeMatrixClient(
            canDeactivateAccountResult = { true },
            accountManagementUrlResult = accountManagementUrlResult,
        )
        createPreferencesAccountPresenter(
            matrixClient = matrixClient,
            sessionEnterpriseService = FakeSessionEnterpriseService(
                tweakMasUrlResult = { "tweaked $it" },
            ),
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.myUser).isEqualTo(
                MatrixUser(
                    userId = matrixClient.sessionId,
                    displayName = A_USER_NAME,
                    avatarUrl = AN_AVATAR_URL
                )
            )
            assertThat(initialState.showSecureBackupBadge).isFalse()
            assertThat(initialState.accountManagementUrl).isNull()
            assertThat(initialState.showLinkNewDevice).isFalse()
            assertThat(initialState.snackbarMessage).isNull()
            val loadedState = consumeItemsUntilPredicate { it.accountManagementUrl != null }.last()
            assertThat(loadedState.showSecureBackup).isFalse()
            assertThat(loadedState.canDeactivateAccount).isTrue()
            assertThat(loadedState.canReportBug).isTrue()
            assertThat(loadedState.accountManagementUrl).isEqualTo("tweaked null url")
            accountManagementUrlResult.assertions().isCalledOnce()
                .with(value(null))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - cannot report bug`() = runTest {
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success("") },
            ),
            rageshakeFeatureAvailability = { flowOf(false) },
        ).test {
            assertThat(awaitItem().canReportBug).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - secure backup badge`() = runTest {
        val indicatorService = FakeIndicatorService()
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success("") },
            ),
            indicatorService = indicatorService,
        ).test {
            assertThat(awaitItem().showSecureBackupBadge).isFalse()
            indicatorService.setShowSettingChatBackupIndicator(true)
            consumeItemsUntilPredicate { it.showSecureBackupBadge }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - secure backup is shown when the session does not need to be verified`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService()
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success("") },
            ),
            sessionVerificationService = sessionVerificationService,
        ).test {
            // The session needs to be verified, so the secure backup entry is hidden.
            consumeItemsUntilPredicate { !it.showSecureBackup }
            sessionVerificationService.emitNeedsSessionVerification(false)
            consumeItemsUntilPredicate { it.showSecureBackup }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - can deactivate account is false if the Matrix client say so`() = runTest {
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                canDeactivateAccountResult = { false },
                accountManagementUrlResult = { Result.success(null) },
            ),
        ).test {
            assertThat(awaitItem().canDeactivateAccount).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - link new device`() = runTest {
        createPreferencesAccountPresenter(
            matrixClient = FakeMatrixClient(
                sessionId = A_SESSION_ID,
                canDeactivateAccountResult = { true },
                accountManagementUrlResult = { Result.success(null) },
            ),
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.QrCodeLogin.key to true)
            ),
        ).test {
            consumeItemsUntilPredicate { it.showLinkNewDevice }
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createPreferencesAccountPresenter(
        matrixClient: FakeMatrixClient = FakeMatrixClient(),
        sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
        rageshakeFeatureAvailability: RageshakeFeatureAvailability = RageshakeFeatureAvailability { flowOf(true) },
        indicatorService: IndicatorService = FakeIndicatorService(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        sessionEnterpriseService: SessionEnterpriseService = FakeSessionEnterpriseService(),
    ) = PreferencesAccountPresenter(
        matrixClient = matrixClient,
        sessionVerificationService = sessionVerificationService,
        snackbarDispatcher = SnackbarDispatcher(),
        indicatorService = indicatorService,
        rageshakeFeatureAvailability = rageshakeFeatureAvailability,
        featureFlagService = featureFlagService,
        sessionEnterpriseService = sessionEnterpriseService,
    )
}
