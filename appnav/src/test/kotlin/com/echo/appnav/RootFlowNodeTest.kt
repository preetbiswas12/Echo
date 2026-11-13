/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.echo.appnav

import android.content.Intent
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.AncestryInfo
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.bumble.appyx.testing.unit.common.helper.parentNodeTestHelper
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectoryImpl
import com.echo.appnav.intent.IntentResolver
import com.echo.appnav.root.RootNavStateFlowFactory
import com.echo.appnav.root.RootPresenter
import com.echo.appnav.session.FakeSyncOrchestratorFactory
import com.echo.appnav.session.MatrixSessionCache
import com.echo.appnav.verification.IncomingVerificationRequestObserver
import com.echo.features.announcement.test.FakeAnnouncementService
import com.echo.features.login.api.LoginEntryPoint
import com.echo.features.login.api.LoginParams
import com.echo.features.login.api.accesscontrol.AccountProviderAccessControl
import com.echo.features.login.test.FakeLoginEntryPoint
import com.echo.features.login.test.FakeLoginIntentResolver
import com.echo.features.login.test.accesscontrol.FakeAccountProviderAccessControl
import com.echo.features.preferences.test.FakeCacheService
import com.echo.features.rageshake.test.FakeBugReportEntryPoint
import com.echo.features.share.api.ShareEntryPoint
import com.echo.features.share.test.FakeShareIntentHandler
import com.echo.features.signedout.test.FakeSignedOutEntryPoint
import com.echo.libraries.accountselect.test.FakeAccountSelectEntryPoint
import com.echo.libraries.architecture.AssistedNodeFactory
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.featureflag.test.FakeFeatureFlagService
import com.echo.libraries.matrix.test.FakeSdkMetadata
import com.echo.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import com.echo.libraries.matrix.test.permalink.FakePermalinkParser
import com.echo.libraries.matrixmedia.test.FakeImageLoaderHolder
import com.echo.libraries.oauth.test.FakeOAuthActionFlow
import com.echo.libraries.oauth.test.FakeOAuthIntentResolver
import com.echo.libraries.preferences.test.FakeSessionPreferencesStoreFactory
import com.echo.libraries.sessionstorage.api.SessionStore
import com.echo.libraries.sessionstorage.test.InMemorySessionStore
import com.echo.libraries.sessionstorage.test.aSessionData
import com.echo.services.analytics.test.FakeAnalyticsService
import com.echo.services.analytics.test.watchers.FakeAnalyticsColdStartWatcher
import com.echo.services.apperror.test.FakeAppErrorStateService
import com.echo.services.appnavstate.test.FakeAppForegroundStateService
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import com.echo.tests.testutils.node.FakeNodeFactoriesBindings
import com.echo.tests.testutils.node.FakeParentNode
import com.echo.tests.testutils.presenter.NotUsedPresenter
import com.echo.tests.testutils.robolectric.RobolectricTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val A_LOGIN_LINK = "https://mobile.element.io/element/?account_provider=example.com&login_hint=mxid:@alice:example.com"

private val A_LOGIN_PARAMS = LoginParams(
    accountProvider = "example.com",
    loginHint = "mxid:@alice:example.com",
)

private val A_LOGIN_ENTRY_POINT_PARAMS = LoginEntryPoint.Params(
    accountProvider = A_LOGIN_PARAMS.accountProvider,
    loginHint = A_LOGIN_PARAMS.loginHint,
)

class RootFlowNodeTest : RobolectricTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `given no session, when a login link is handled before the nav state is observed, then the login params are kept`() = runTest {
        var loginEntryPointParams: LoginEntryPoint.Params? = null
        val rootFlowNode = createRootFlowNode(
            loginEntryPoint = FakeLoginEntryPoint { buildContext, params ->
                loginEntryPointParams = params
                node(buildContext) {}
            },
        )
        rootFlowNode.parentNodeTestHelper()
        // The intent is handled first, this is what happens when the deep link cold starts the app.
        rootFlowNode.handleIntent(aLoginIntent())
        assertThat(rootFlowNode.backstack.activeElement).isEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(A_LOGIN_PARAMS))
        // Then the first nav state emission lands, it must not override the login params.
        runCurrent()
        assertThat(rootFlowNode.backstack.activeElement).isEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(A_LOGIN_PARAMS))
        assertThat(loginEntryPointParams).isEqualTo(A_LOGIN_ENTRY_POINT_PARAMS)
    }

    @Test
    fun `given no session, when a login link is handled after the nav state is observed, then the login params are applied`() = runTest {
        var loginEntryPointParams: LoginEntryPoint.Params? = null
        val rootFlowNode = createRootFlowNode(
            loginEntryPoint = FakeLoginEntryPoint { buildContext, params ->
                loginEntryPointParams = params
                node(buildContext) {}
            },
        )
        rootFlowNode.parentNodeTestHelper()
        // The nav state is observed first, this is what happens when the app is already running.
        runCurrent()
        assertThat(rootFlowNode.backstack.activeElement).isEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(null))
        rootFlowNode.handleIntent(aLoginIntent())
        runCurrent()
        assertThat(rootFlowNode.backstack.activeElement).isEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(A_LOGIN_PARAMS))
        assertThat(loginEntryPointParams).isEqualTo(A_LOGIN_ENTRY_POINT_PARAMS)
    }

    @Test
    fun `given a session matching the login hint and multi account enabled, when a login link is handled, then the session becomes the latest one`() = runTest {
        val setLatestSessionResult = lambdaRecorder<String, Unit> { }
        val rootFlowNode = createRootFlowNode(
            loginEntryPoint = FakeLoginEntryPoint(),
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = "@bob:example.com"),
                    aSessionData(sessionId = "@alice:example.com"),
                ),
                setLatestSessionResult = setLatestSessionResult,
            ),
            featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.MultiAccount.key to true)),
        )
        rootFlowNode.parentNodeTestHelper()
        rootFlowNode.handleIntent(aLoginIntent())
        setLatestSessionResult.assertions().isCalledOnce().with(value("@alice:example.com"))
        assertThat(rootFlowNode.backstack.activeElement).isNotEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(A_LOGIN_PARAMS))
    }

    @Test
    fun `given a session and multi account disabled, when a login link is handled, then the login link is ignored`() = runTest {
        val rootFlowNode = createRootFlowNode(
            loginEntryPoint = FakeLoginEntryPoint(),
            sessionStore = InMemorySessionStore(
                initialList = listOf(aSessionData(sessionId = "@alice:example.com")),
            ),
            featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.MultiAccount.key to false)),
        )
        rootFlowNode.parentNodeTestHelper()
        rootFlowNode.handleIntent(aLoginIntent())
        assertThat(rootFlowNode.backstack.activeElement).isNotEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(A_LOGIN_PARAMS))
    }

    @Test
    fun `given no session, when a login link to a not allowed account provider is handled, then the login link is ignored`() = runTest {
        val rootFlowNode = createRootFlowNode(
            loginEntryPoint = FakeLoginEntryPoint { buildContext, _ -> node(buildContext) {} },
            accountProviderAccessControl = FakeAccountProviderAccessControl { false },
        )
        rootFlowNode.parentNodeTestHelper()
        rootFlowNode.handleIntent(aLoginIntent())
        runCurrent()
        assertThat(rootFlowNode.backstack.activeElement).isEqualTo(RootFlowNode.NavTarget.NotLoggedInFlow(null))
    }

    private fun aLoginIntent() = Intent(Intent.ACTION_VIEW, Uri.parse(A_LOGIN_LINK))

    private fun TestScope.createRootFlowNode(
        loginEntryPoint: LoginEntryPoint,
        sessionStore: SessionStore = InMemorySessionStore(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        accountProviderAccessControl: AccountProviderAccessControl = FakeAccountProviderAccessControl { true },
    ): RootFlowNode {
        val matrixSessionCache = MatrixSessionCache(
            authenticationService = FakeMatrixAuthenticationService(),
            syncOrchestratorFactory = FakeSyncOrchestratorFactory(),
            analyticsService = FakeAnalyticsService(),
        )
        val parentNode = FakeParentNode(
            graph = FakeNodeFactoriesBindings(
                mapOf(
                    NotLoggedInFlowNode::class to AssistedNodeFactory { buildContext, plugins ->
                        NotLoggedInFlowNode(
                            buildContext = buildContext,
                            plugins = plugins,
                            loginEntryPoint = loginEntryPoint,
                            imageLoaderHolder = FakeImageLoaderHolder(),
                            analyticsColdStartWatcher = FakeAnalyticsColdStartWatcher(),
                            featureFlagService = FakeFeatureFlagService(),
                            announcementService = FakeAnnouncementService(),
                        )
                    }
                )
            )
        )
        return RootFlowNode(
            buildContext = BuildContext(
                ancestryInfo = AncestryInfo.Child(anchor = parentNode),
                savedStateMap = null,
                customisations = NodeCustomisationDirectoryImpl(),
            ),
            plugins = emptyList(),
            sessionStore = sessionStore,
            accountProviderAccessControl = accountProviderAccessControl,
            navStateFlowFactory = RootNavStateFlowFactory(
                sessionStore = sessionStore,
                cacheService = FakeCacheService(),
                matrixSessionCache = matrixSessionCache,
                imageLoaderHolder = FakeImageLoaderHolder(),
                sessionPreferencesStoreFactory = FakeSessionPreferencesStoreFactory(),
            ),
            matrixSessionCache = matrixSessionCache,
            presenter = RootPresenter(
                crashDetectionPresenter = NotUsedPresenter(),
                rageshakeDetectionPresenter = NotUsedPresenter(),
                appErrorStateService = FakeAppErrorStateService(),
                analyticsService = FakeAnalyticsService(),
                sdkMetadata = FakeSdkMetadata("sha"),
            ),
            bugReportEntryPoint = FakeBugReportEntryPoint(),
            signedOutEntryPoint = FakeSignedOutEntryPoint(),
            accountSelectEntryPoint = FakeAccountSelectEntryPoint(),
            intentResolver = IntentResolver(
                deeplinkParser = { null },
                loginIntentResolver = FakeLoginIntentResolver { A_LOGIN_PARAMS },
                oAuthIntentResolver = FakeOAuthIntentResolver { null },
                permalinkParser = FakePermalinkParser(),
                shareIntentHandler = FakeShareIntentHandler(),
            ),
            oAuthActionFlow = FakeOAuthActionFlow(),
            featureFlagService = featureFlagService,
            announcementService = FakeAnnouncementService(),
            analyticsService = FakeAnalyticsService(),
            analyticsColdStartWatcher = FakeAnalyticsColdStartWatcher(),
            incomingVerificationRequestObserver = IncomingVerificationRequestObserver(
                matrixSessionCache = matrixSessionCache,
                appForegroundStateService = FakeAppForegroundStateService(),
            ),
            appCoroutineScope = backgroundScope,
            shareEntryPoint = object : ShareEntryPoint {
                override fun createNode(
                    parentNode: Node,
                    buildContext: BuildContext,
                    params: ShareEntryPoint.Params,
                    callback: ShareEntryPoint.Callback,
                ): Node = lambdaError()
            },
        )
    }
}
