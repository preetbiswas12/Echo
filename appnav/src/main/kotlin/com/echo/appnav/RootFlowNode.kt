/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.appnav

import android.content.Intent
import android.os.Parcelable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.NavElements
import com.bumble.appyx.core.navigation.NavKey
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.state.MutableSavedStateMap
import com.bumble.appyx.core.state.SavedStateMap
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
import com.echo.annotations.ContributesNode
import com.echo.appnav.intent.IntentResolver
import com.echo.appnav.intent.ResolvedIntent
import com.echo.appnav.room.RoomFlowNode
import com.echo.appnav.room.RoomNavigationTarget
import com.echo.appnav.root.RootNavStateFlowFactory
import com.echo.appnav.root.RootPresenter
import com.echo.appnav.root.RootView
import com.echo.appnav.session.MatrixSessionCache
import com.echo.appnav.verification.IncomingVerificationRequestData
import com.echo.appnav.verification.IncomingVerificationRequestObserver
import com.echo.appnav.verification.OtherSessionIncomingVerificationNode
import com.echo.appnav.verification.OtherSessionVerificationNode
import com.echo.features.announcement.api.AnnouncementService
import com.echo.features.login.api.LoginParams
import com.echo.features.login.api.accesscontrol.AccountProviderAccessControl
import com.echo.features.rageshake.api.bugreport.BugReportEntryPoint
import com.echo.features.share.api.ShareEntryPoint
import com.echo.features.share.api.ShareIntentData
import com.echo.features.signedout.api.SignedOutEntryPoint
import com.echo.libraries.accountselect.api.AccountSelectEntryPoint
import com.echo.libraries.architecture.BackstackView
import com.echo.libraries.architecture.BaseFlowNode
import com.echo.libraries.architecture.appyx.rememberDelegateTransitionHandler
import com.echo.libraries.architecture.createNode
import com.echo.libraries.architecture.waitForChildAttached
import com.echo.libraries.core.coroutine.withPreviousValue
import com.echo.libraries.deeplink.api.DeeplinkData
import com.echo.libraries.di.annotations.AppCoroutineScope
import com.echo.libraries.featureflag.api.FeatureFlagService
import com.echo.libraries.featureflag.api.FeatureFlags
import com.echo.libraries.matrix.api.accountprovider.AccountProvider
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.core.RoomId
import com.echo.libraries.matrix.api.core.SessionId
import com.echo.libraries.matrix.api.core.ThreadId
import com.echo.libraries.matrix.api.core.asEventId
import com.echo.libraries.matrix.api.core.toRoomIdOrAlias
import com.echo.libraries.matrix.api.permalink.PermalinkData
import com.echo.libraries.matrix.api.verification.VerificationRequest
import com.echo.libraries.oauth.api.OAuthAction
import com.echo.libraries.oauth.api.OAuthActionFlow
import com.echo.libraries.sessionstorage.api.LoggedInState
import com.echo.libraries.sessionstorage.api.SessionStore
import com.echo.libraries.ui.common.nodes.emptyNode
import com.echo.services.analytics.api.AnalyticsLongRunningTransaction
import com.echo.services.analytics.api.AnalyticsService
import com.echo.services.analytics.api.watchers.AnalyticsColdStartWatcher
import com.echo.services.appnavstate.api.ROOM_OPENED_FROM_NOTIFICATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@ContributesNode(AppScope::class)
@AssistedInject
class RootFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val sessionStore: SessionStore,
    private val accountProviderAccessControl: AccountProviderAccessControl,
    private val navStateFlowFactory: RootNavStateFlowFactory,
    private val matrixSessionCache: MatrixSessionCache,
    private val presenter: RootPresenter,
    private val bugReportEntryPoint: BugReportEntryPoint,
    private val signedOutEntryPoint: SignedOutEntryPoint,
    private val accountSelectEntryPoint: AccountSelectEntryPoint,
    private val intentResolver: IntentResolver,
    private val oAuthActionFlow: OAuthActionFlow,
    private val featureFlagService: FeatureFlagService,
    private val announcementService: AnnouncementService,
    private val analyticsService: AnalyticsService,
    private val analyticsColdStartWatcher: AnalyticsColdStartWatcher,
    private val incomingVerificationRequestObserver: IncomingVerificationRequestObserver,
    private val shareEntryPoint: ShareEntryPoint,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : BaseFlowNode<RootFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.SplashScreen,
        savedStateMap = null,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    /**
     * Login params coming from a launch or new [Intent], waiting to be consumed by the not logged in flow.
     * Kept here so that the root nav target can be computed from both the logged in state and the pending
     * login params, whatever the order in which the intent and the first nav state emission are processed.
     */
    private var pendingLoginParams: LoginParams? = null

    override fun onBuilt() {
        analyticsColdStartWatcher.start()
        appCoroutineScope.launch {
            matrixSessionCache.restoreWithSavedState(buildContext.savedStateMap)
            if (buildContext.savedStateMap != null) {
                restoreSavedState(buildContext.savedStateMap)
                observeNavState(true)
            } else {
                observeNavState(false)
            }
        }
        observeIncomingVerificationRequests()
        observeRemovedSessions()
        super.onBuilt()
    }

    override fun onSaveInstanceState(state: MutableSavedStateMap) {
        super.onSaveInstanceState(state)
        matrixSessionCache.saveIntoSavedState(state)
        navStateFlowFactory.saveIntoSavedState(state)
    }

    private fun observeNavState(skipFirst: Boolean) {
        navStateFlowFactory.create(buildContext.savedStateMap)
            .distinctUntilChanged()
            .drop(if (skipFirst) 1 else 0)
            .onEach { navState ->
                Timber.v("navState=$navState")
                when (navState.loggedInState) {
                    is LoggedInState.LoggedIn -> {
                        if (navState.loggedInState.isTokenValid) {
                            val sessionId = SessionId(navState.loggedInState.sessionId)
                            if (matrixSessionCache.getOrNull(sessionId) != null) {
                                switchToLoggedInFlow(sessionId, navState.cacheIndex)
                            } else {
                                tryToRestoreLatestSession(
                                    onSuccess = { sessionId -> switchToLoggedInFlow(sessionId, navState.cacheIndex) },
                                    onFailure = { switchToNotLoggedInFlow(null) }
                                )
                            }
                        } else {
                            switchToSignedOutFlow(SessionId(navState.loggedInState.sessionId))
                        }
                    }
                    LoggedInState.NotLoggedIn -> {
                        switchToNotLoggedInFlow(pendingLoginParams)
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    /**
     * Restore the saved state for navigation in the current backstack.
     *
     * **WARNING:** this is an unsafe operation abusing the internals of the Appyx library, but it's the only way allow async state
     * restoration and not having to block the main thread when the app starts.
     *
     * Modify with utmost care and double check any possible Appyx updates that might break this.
     */
    @Suppress("UNCHECKED_CAST")
    private fun restoreSavedState(savedStateMap: SavedStateMap?) {
        if (savedStateMap == null) return

        // 'NavModel' is the key used for storing the nav model state data in the map in Appyx
        val savedElements = buildContext.savedStateMap?.get("NavModel") as? NavElements<NavTarget, BackStack.State>
        if (savedElements != null) {
            backstack.accept(ReplaceAllOperation(savedElements))
        }
    }

    /**
     * Extract the saved state for navigation in the [navTarget].
     *
     * **WARNING:** this is an unsafe operation abusing the internals of the Appyx library, but it's the only way allow async state
     * restoration and not having to block the main thread when the app starts.
     *
     * Modify with utmost care and double check any possible Appyx updates that might break this.
     */
    @Suppress("UNCHECKED_CAST")
    private fun extractSavedStateForNavTarget(navTarget: NavTarget, savedStateMap: SavedStateMap?): SavedStateMap? {
        // 'ChildrenState' is the key used for storing the children state data in the map in Appyx
        val childrenState = savedStateMap?.get("ChildrenState") as? Map<NavKey<NavTarget>, SavedStateMap> ?: return null
        return childrenState.entries.find { (key, _) -> key.navTarget == navTarget }?.value
    }

    private fun switchToLoggedInFlow(sessionId: SessionId, navId: Int) {
        pendingLoginParams = null
        setRoot(NavTarget.LoggedInFlow(sessionId, navId))
        restoreAllSessions()
    }

    /**
     * Restore all the sessions with a valid token, so that they sync and can receive incoming verification requests.
     */
    private fun restoreAllSessions() = lifecycleScope.launch {
        sessionStore.getAllSessions()
            .filter { it.isTokenValid }
            .forEach { sessionData ->
                matrixSessionCache.getOrRestore(SessionId(sessionData.userId)).onFailure {
                    Timber.e(it, "Failed to restore session ${sessionData.userId}")
                }
            }
    }

    /**
     * Remove from the cache the sessions which have been removed from the session store, for instance after a logout.
     */
    private fun observeRemovedSessions() {
        sessionStore.sessionsFlow()
            .map { sessions -> sessions.map { SessionId(it.userId) }.toSet() }
            .withPreviousValue()
            .onEach { (previous, current) ->
                (previous.orEmpty() - current).forEach { sessionId ->
                    // The verification of a removed session cannot continue, for instance if the user has signed out from it
                    backstack.removeLast(NavTarget.OtherAccountSessionVerification(sessionId))
                    Timber.d("Session $sessionId removed, remove it from the cache")
                    matrixSessionCache.remove(sessionId)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun observeIncomingVerificationRequests() {
        incomingVerificationRequestObserver.incomingVerificationRequests()
            .onEach(::onIncomingVerificationRequest)
            .launchIn(lifecycleScope)
    }

    private fun onIncomingVerificationRequest(data: IncomingVerificationRequestData) = lifecycleScope.launch {
        // Wait for a logged in flow to be displayed
        val currentSessionId = withTimeoutOrNull(5.seconds) {
            backstack.elements.map { elements ->
                elements.firstNotNullOfOrNull { (it.key.navTarget as? NavTarget.LoggedInFlow)?.sessionId }
            }.filterNotNull().first()
        }
        if (currentSessionId == null) {
            Timber.w("Incoming verification request ${data.verificationRequest.details.flowId} discarded, no logged in flow.")
            return@launch
        }
        if (currentSessionId == data.sessionId) {
            waitForChildAttached<LoggedInAppScopeFlowNode, NavTarget> { navTarget ->
                navTarget is NavTarget.LoggedInFlow && navTarget.sessionId == currentSessionId
            }
                .attachSession()
                .onIncomingVerificationRequest(data.verificationRequest)
        } else {
            // Display the request on top of the current session, without switching session
            backstack.push(NavTarget.OtherAccountIncomingVerificationRequest(data.sessionId, data.verificationRequest))
        }
    }

    private fun switchToNotLoggedInFlow(params: LoginParams?) {
        Timber.d("switchToNotLoggedInFlow, hasLoginParams=${params != null}")
        matrixSessionCache.removeAll()
        backstack.safeRoot(NavTarget.NotLoggedInFlow(params))
    }

    private fun switchToSignedOutFlow(sessionId: SessionId) {
        pendingLoginParams = null
        setRoot(NavTarget.SignedOutFlow(sessionId))
    }

    /**
     * Set the root of the backstack. If an incoming share is displayed, keep it on top of the new root, so that the user can
     * finish sharing, for instance with another account if the current one has been signed out.
     */
    private fun setRoot(navTarget: NavTarget) {
        val hasIncomingShare = backstack.elements.value.any { it.key.navTarget is NavTarget.IncomingShare }
        if (hasIncomingShare) {
            backstack.replaceRoot(navTarget)
        } else {
            backstack.safeRoot(navTarget)
        }
    }

    private suspend fun restoreSessionIfNeeded(
        sessionId: SessionId,
        onFailure: () -> Unit,
        onSuccess: (SessionId) -> Unit,
    ) {
        matrixSessionCache.getOrRestore(sessionId).onSuccess {
            Timber.v("Succeed to restore session $sessionId")
            onSuccess(sessionId)
        }.onFailure {
            Timber.e(it, "Failed to restore session $sessionId")
            onFailure()
        }
    }

    private suspend fun tryToRestoreLatestSession(
        onSuccess: (SessionId) -> Unit, onFailure: () -> Unit
    ) {
        val latestSessionId = sessionStore.getLatestSessionId()
        if (latestSessionId == null) {
            onFailure()
            return
        }
        restoreSessionIfNeeded(latestSessionId, onFailure, onSuccess)
    }

    private fun onOpenBugReport() {
        backstack.push(NavTarget.BugReport)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RootView(
            state = state,
            modifier = modifier,
            onOpenBugReport = this::onOpenBugReport,
        ) {
            val backstackSlider = rememberBackstackSlider<NavTarget>(
                transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
            )
            val backstackFader = rememberBackstackFader<NavTarget>(
                transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
            )
            val transitionHandler = rememberDelegateTransitionHandler<NavTarget, BackStack.State> { navTarget ->
                when (navTarget) {
                    is NavTarget.SplashScreen,
                    is NavTarget.LoggedInFlow,
                    is NavTarget.NotLoggedInFlow -> backstackFader
                    else -> backstackSlider
                }
            }
            BackstackView(transitionHandler = transitionHandler)
            announcementService.Render(Modifier)
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize data object SplashScreen : NavTarget

        @Parcelize data class AccountSelect(
            val currentSessionId: SessionId,
            val permalinkData: PermalinkData,
        ) : NavTarget

        @Parcelize data class NotLoggedInFlow(
            val params: LoginParams?
        ) : NavTarget

        @Parcelize data class LoggedInFlow(
            val sessionId: SessionId, val navId: Int
        ) : NavTarget

        @Parcelize data class SignedOutFlow(
            val sessionId: SessionId
        ) : NavTarget

        @Parcelize data object BugReport : NavTarget

        @Parcelize data class IncomingShare(
            val sessionId: SessionId,
            val shareIntentData: ShareIntentData,
        ) : NavTarget

        @Parcelize data class OtherAccountIncomingVerificationRequest(
            val sessionId: SessionId,
            val verificationRequest: VerificationRequest.Incoming,
        ) : NavTarget

        @Parcelize data class OtherAccountSessionVerification(
            val sessionId: SessionId,
        ) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.LoggedInFlow -> {
                val matrixClient = matrixSessionCache.getOrNull(navTarget.sessionId)
                    ?: return emptyNode(buildContext).also {
                        Timber.w("Couldn't find any session, go through SplashScreen")
                    }
                val inputs = LoggedInAppScopeFlowNode.Inputs(matrixClient)
                val callback = object : LoggedInAppScopeFlowNode.Callback {
                    override fun navigateToBugReport() {
                        backstack.push(NavTarget.BugReport)
                    }

                    override fun navigateToAddAccount() {
                        backstack.push(NavTarget.NotLoggedInFlow(null))
                    }

                    override fun navigateToSession(sessionId: SessionId) {
                        lifecycleScope.launch {
                            sessionStore.setLatestSession(sessionId.value)
                        }
                    }
                }
                val savedNavState = extractSavedStateForNavTarget(navTarget, this.buildContext.savedStateMap)
                val buildContext = if (savedNavState != null) {
                    Timber.d("Creating a $navTarget with restored saved state")
                    buildContext.copy(savedStateMap = savedNavState)
                } else {
                    buildContext.copy(savedStateMap = savedNavState)
                }
                createNode<LoggedInAppScopeFlowNode>(buildContext, plugins = listOf(inputs, callback))
            }
            is NavTarget.NotLoggedInFlow -> {
                val callback = object : NotLoggedInFlowNode.Callback {
                    override fun navigateToBugReport() {
                        backstack.push(NavTarget.BugReport)
                    }

                    override fun onDone() {
                        pendingLoginParams = null
                        backstack.pop()
                    }
                }
                val params = NotLoggedInFlowNode.Params(
                    loginParams = navTarget.params,
                )
                createNode<NotLoggedInFlowNode>(buildContext, plugins = listOf(params, callback))
            }
            is NavTarget.SignedOutFlow -> {
                signedOutEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = SignedOutEntryPoint.Params(
                        sessionId = navTarget.sessionId,
                    ),
                )
            }
            NavTarget.SplashScreen -> emptyNode(buildContext)
            is NavTarget.OtherAccountIncomingVerificationRequest -> {
                if (matrixSessionCache.getOrNull(navTarget.sessionId) == null) {
                    Timber.w("Couldn't find session ${navTarget.sessionId} for the incoming verification request")
                    lifecycleScope.launch { backstack.pop() }
                    return emptyNode(buildContext)
                }
                val inputs = OtherSessionIncomingVerificationNode.Inputs(
                    sessionId = navTarget.sessionId,
                    verificationRequest = navTarget.verificationRequest,
                )
                val callback = object : OtherSessionIncomingVerificationNode.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                createNode<OtherSessionIncomingVerificationNode>(buildContext, plugins = listOf(inputs, callback))
            }
            is NavTarget.OtherAccountSessionVerification -> {
                if (matrixSessionCache.getOrNull(navTarget.sessionId) == null) {
                    Timber.w("Couldn't find session ${navTarget.sessionId} to verify")
                    lifecycleScope.launch { backstack.pop() }
                    return emptyNode(buildContext)
                }
                val inputs = OtherSessionVerificationNode.Inputs(sessionId = navTarget.sessionId)
                val callback = object : OtherSessionVerificationNode.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                createNode<OtherSessionVerificationNode>(buildContext, plugins = listOf(inputs, callback))
            }
            is NavTarget.IncomingShare -> {
                val callback = object : ShareEntryPoint.Callback {
                    override fun onDone(sessionId: SessionId, roomIds: List<RoomId>) {
                        // Remove the incoming share screen
                        backstack.pop()
                        lifecycleScope.launch {
                            // Switch to the session which has been used to share the data, if necessary,
                            // and navigate to the room if the data was shared to a single one.
                            val loggedInFlowNode = attachSession(sessionId)
                            roomIds.singleOrNull()?.let { roomId ->
                                loggedInFlowNode.attachRoom(roomId.toRoomIdOrAlias(), clearBackstack = false)
                            }
                        }
                    }

                    override fun onCancel() {
                        backstack.pop()
                    }

                    override fun onSessionVerificationRequired(sessionId: SessionId) {
                        val navTarget = NavTarget.OtherAccountSessionVerification(sessionId)
                        if (backstack.elements.value.lastOrNull()?.key?.navTarget != navTarget) {
                            backstack.push(navTarget)
                        }
                    }
                }
                shareEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = ShareEntryPoint.Params(
                        sessionId = navTarget.sessionId,
                        shareIntentData = navTarget.shareIntentData,
                    ),
                    callback = callback,
                )
            }
            NavTarget.BugReport -> {
                val callback = object : BugReportEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                bugReportEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            is NavTarget.AccountSelect -> {
                val callback: AccountSelectEntryPoint.Callback = object : AccountSelectEntryPoint.Callback {
                    override fun onAccountSelected(sessionId: SessionId) {
                        lifecycleScope.launch {
                            if (sessionId == navTarget.currentSessionId) {
                                // Ensure that the account selection Node is removed from the backstack
                                // Do not pop when the account is changed to avoid a UI flicker.
                                backstack.pop()
                            }
                            attachSession(sessionId).attachPermalinkData(navTarget.permalinkData)
                        }
                    }

                    override fun onCancel() {
                        backstack.pop()
                    }
                }
                accountSelectEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
        }
    }

    suspend fun handleIntent(intent: Intent) {
        val resolvedIntent = intentResolver.resolve(intent) ?: return
        when (resolvedIntent) {
            is ResolvedIntent.Navigation -> {
                val openingRoomFromNotification = intent.getBooleanExtra(ROOM_OPENED_FROM_NOTIFICATION, false)
                if (openingRoomFromNotification && resolvedIntent.deeplinkData is DeeplinkData.Room) {
                    analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.NotificationToMessage)
                }
                navigateTo(resolvedIntent.deeplinkData)
            }
            is ResolvedIntent.Login -> onLoginLink(resolvedIntent.params)
            is ResolvedIntent.OAuth -> onOAuthAction(resolvedIntent.oAuthAction)
            is ResolvedIntent.Permalink -> navigateTo(resolvedIntent.permalinkData)
            is ResolvedIntent.IncomingShare -> onIncomingShare(resolvedIntent.shareIntentData)
        }
    }

    private suspend fun onLoginLink(params: LoginParams) {
        val accountProvider = AccountProvider.Generic(params.accountProvider)
        if (accountProviderAccessControl.isAllowedToConnectToAccountProvider(accountProvider)) {
            // Is there a session already?
            val sessions = sessionStore.getAllSessions()
            if (sessions.isNotEmpty()) {
                if (featureFlagService.isFeatureEnabled(FeatureFlags.MultiAccount)) {
                    val loginHintMatrixId = params.loginHint?.removePrefix("mxid:")
                    val existingAccount = sessions.find { it.userId == loginHintMatrixId }
                    if (existingAccount != null) {
                        // We have an existing account matching the login hint, ensure this is the current session
                        sessionStore.setLatestSession(existingAccount.userId)
                    } else {
                        val latestSessionId = sessions.maxBy { it.lastUsageIndex }.userId
                        attachSession(SessionId(latestSessionId))
                        backstack.push(NavTarget.NotLoggedInFlow(params))
                    }
                } else {
                    Timber.w("Login link ignored, multi account is disabled")
                }
            } else {
                pendingLoginParams = params
                switchToNotLoggedInFlow(params)
            }
        } else {
            Timber.w("Login link ignored, we are not allowed to connect to the homeserver")
        }
    }

    private suspend fun onIncomingShare(shareIntentData: ShareIntentData) {
        // Is there a session already?
        val latestSessionId = sessionStore.getLatestSessionId()
        if (latestSessionId == null) {
            // No session, open login
            switchToNotLoggedInFlow(null)
        } else {
            // Wait for the current session to be restored and for the FTUE to be complete, so that the session is verified,
            // then display the incoming share on top of it.
            // The incoming share does not belong to the current session: in case of multiple accounts, the user can switch account
            // from the room select screen, and the share is kept if the current session is signed out in the meantime.
            attachSession(latestSessionId).waitForHome()
            backstack.push(NavTarget.IncomingShare(latestSessionId, shareIntentData))
        }
    }

    private suspend fun navigateTo(permalinkData: PermalinkData) {
        Timber.d("Navigating to $permalinkData")
        // Is there a session already?
        val latestSessionId = sessionStore.getLatestSessionId()
        if (latestSessionId == null) {
            // No session, open login
            switchToNotLoggedInFlow(null)
        } else {
            // wait for the current session to be restored
            val loggedInFlowNode = attachSession(latestSessionId)
            when (permalinkData) {
                is PermalinkData.FallbackLink -> Unit
                is PermalinkData.RoomEmailInviteLink -> Unit
                else -> {
                    if (sessionStore.numberOfSessions() > 1) {
                        // Several accounts, let the user choose which one to use
                        backstack.push(
                            NavTarget.AccountSelect(
                                currentSessionId = latestSessionId,
                                permalinkData = permalinkData,
                            )
                        )
                    } else {
                        // Only one account, directly attach the room or the user node.
                        loggedInFlowNode.attachPermalinkData(permalinkData)
                    }
                }
            }
        }
    }

    private suspend fun LoggedInFlowNode.attachPermalinkData(permalinkData: PermalinkData) {
        when (permalinkData) {
            is PermalinkData.FallbackLink -> Unit
            is PermalinkData.RoomEmailInviteLink -> Unit
            is PermalinkData.RoomLink -> {
                // If there is a thread id, focus on it in the main timeline
                val focusedEventId = if (permalinkData.threadId != null) {
                    permalinkData.threadId?.asEventId()
                } else {
                    permalinkData.eventId
                }
                attachRoom(
                    roomIdOrAlias = permalinkData.roomIdOrAlias,
                    trigger = JoinedRoom.Trigger.MobilePermalink,
                    serverNames = permalinkData.viaParameters,
                    initialElement = RoomNavigationTarget.Root(eventId = focusedEventId),
                    clearBackstack = true
                ).maybeAttachThread(permalinkData.threadId, permalinkData.eventId)
            }
            is PermalinkData.UserLink -> {
                attachUser(permalinkData.userId)
            }
        }
    }

    private suspend fun RoomFlowNode.maybeAttachThread(threadId: ThreadId?, focusedEventId: EventId?) {
        if (threadId != null) {
            attachThread(threadId, focusedEventId)
        }
    }

    private suspend fun navigateTo(deeplinkData: DeeplinkData) {
        Timber.d("Navigating to $deeplinkData")
        attachSession(deeplinkData.sessionId).let { loggedInFlowNode ->
            when (deeplinkData) {
                is DeeplinkData.Root -> Unit // The room list will always be shown, observing FtueState
                is DeeplinkData.Room -> {
                    loggedInFlowNode.attachRoom(
                        roomIdOrAlias = deeplinkData.roomId.toRoomIdOrAlias(),
                        initialElement = RoomNavigationTarget.Root(eventId = deeplinkData.threadId?.asEventId() ?: deeplinkData.eventId),
                        clearBackstack = true,
                    ).maybeAttachThread(deeplinkData.threadId, deeplinkData.eventId)
                }
            }
        }
    }

    private fun onOAuthAction(oAuthAction: OAuthAction) {
        oAuthActionFlow.post(oAuthAction)
    }

    private suspend fun attachSession(sessionId: SessionId): LoggedInFlowNode {
        // Ensure that the session is the latest one
        sessionStore.setLatestSession(sessionId.value)
        return waitForChildAttached<LoggedInAppScopeFlowNode, NavTarget> { navTarget ->
            navTarget is NavTarget.LoggedInFlow && navTarget.sessionId == sessionId
        }.attachSession()
    }
}

private suspend fun SessionStore.getLatestSessionId() = getLatestSession()?.userId?.let(::SessionId)

