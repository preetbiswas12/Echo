/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.push.impl

import com.echo.libraries.core.meta.BuildMeta
import com.echo.libraries.matrix.api.pusher.SetHttpPusherData
import com.echo.libraries.matrix.api.pusher.UnsetHttpPusherData
import com.echo.libraries.matrix.test.AN_EXCEPTION
import com.echo.libraries.matrix.test.A_SECRET
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.core.aBuildMeta
import com.echo.libraries.matrix.test.pushers.FakePushersService
import com.echo.libraries.pushstore.api.UserPushStoreFactory
import com.echo.libraries.pushstore.api.clientsecret.PushClientSecret
import com.echo.libraries.pushstore.test.userpushstore.FakeUserPushStore
import com.echo.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import com.echo.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import com.echo.tests.testutils.lambda.lambdaRecorder
import com.echo.tests.testutils.lambda.value
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultPusherSubscriberTest {
    @Test
    fun `test register pusher OK`() = runTest {
        testRegisterPusher(
            currentPushKey = null,
            registerResult = Result.success(Unit),
        )
    }

    @Test
    fun `test re-register pusher OK`() = runTest {
        testRegisterPusher(
            currentPushKey = "aPushKey",
            registerResult = Result.success(Unit),
        )
    }

    @Test
    fun `test register pusher error`() = runTest {
        testRegisterPusher(
            currentPushKey = null,
            registerResult = Result.failure(AN_EXCEPTION),
        )
    }

    @Test
    fun `test re-register pusher error`() = runTest {
        testRegisterPusher(
            currentPushKey = "aPushKey",
            registerResult = Result.failure(AN_EXCEPTION),
        )
    }

    private suspend fun testRegisterPusher(
        currentPushKey: String?,
        registerResult: Result<Unit>,
    ) {
        val setHttpPusherResult = lambdaRecorder<SetHttpPusherData, Result<Unit>> { registerResult }
        val userPushStore = FakeUserPushStore().apply {
            setCurrentRegisteredPushKey(currentPushKey)
        }
        assertThat(userPushStore.getCurrentRegisteredPushKey()).isEqualTo(currentPushKey)

        val matrixClient = FakeMatrixClient(
            pushersService = FakePushersService(
                setHttpPusherResult = setHttpPusherResult,
            ),
        )
        val defaultPusherSubscriber = createDefaultPusherSubscriber(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET },
            ),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { userPushStore },
            ),
        )
        val result = defaultPusherSubscriber.registerPusher(
            matrixClient = matrixClient,
            pushKey = "aPushKey",
            gateway = "aGateway",
        )
        assertThat(result).isEqualTo(registerResult)
        setHttpPusherResult.assertions()
            .isCalledOnce()
            .with(
                value(
                    SetHttpPusherData(
                        pushKey = "aPushKey",
                        appId = BuildConfig.PUSHER_APP_ID,
                        url = "aGateway",
                        appDisplayName = "MyApp",
                        deviceDisplayName = "MyDevice",
                        profileTag = null,
                        lang = "en",
                        defaultPayload = """{"cs":"$A_SECRET"}""",
                        append = false,
                    ),
                )
            )
        assertThat(userPushStore.getCurrentRegisteredPushKey()).isEqualTo(
            if (registerResult.isSuccess) "aPushKey" else currentPushKey
        )
    }

    @Test
    fun `test unregister pusher OK`() = runTest {
        testUnregisterPusher(
            currentPushKey = "aPushKey",
            unregisterResult = Result.success(Unit),
        )
    }

    @Test
    fun `test unregister pusher error`() = runTest {
        testUnregisterPusher(
            currentPushKey = "aPushKey",
            unregisterResult = Result.failure(AN_EXCEPTION),
        )
    }

    private suspend fun testUnregisterPusher(
        currentPushKey: String?,
        unregisterResult: Result<Unit>,
    ) {
        val unsetHttpPusherResult = lambdaRecorder<UnsetHttpPusherData, Result<Unit>> { unregisterResult }
        val userPushStore = FakeUserPushStore().apply {
            setCurrentRegisteredPushKey(currentPushKey)
        }
        assertThat(userPushStore.getCurrentRegisteredPushKey()).isEqualTo(currentPushKey)

        val matrixClient = FakeMatrixClient(
            pushersService = FakePushersService(
                unsetHttpPusherResult = unsetHttpPusherResult,
            ),
        )
        val defaultPusherSubscriber = createDefaultPusherSubscriber(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET },
            ),
            userPushStoreFactory = FakeUserPushStoreFactory(
                userPushStore = { userPushStore },
            ),
        )
        val result = defaultPusherSubscriber.unregisterPusher(
            matrixClient = matrixClient,
            pushKey = "aPushKey",
            gateway = "aGateway",
        )
        assertThat(result).isEqualTo(unregisterResult)
        unsetHttpPusherResult.assertions()
            .isCalledOnce()
            .with(
                value(
                    UnsetHttpPusherData(
                        pushKey = "aPushKey",
                        appId = BuildConfig.PUSHER_APP_ID,
                    ),
                )
            )
        assertThat(userPushStore.getCurrentRegisteredPushKey()).isEqualTo(
            if (unregisterResult.isSuccess) null else currentPushKey
        )
    }

    private fun createDefaultPusherSubscriber(
        buildMeta: BuildMeta = aBuildMeta(applicationName = "MyApp"),
        userPushStoreFactory: UserPushStoreFactory = FakeUserPushStoreFactory(),
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
    ): DefaultPusherSubscriber {
        return DefaultPusherSubscriber(
            buildMeta = buildMeta,
            pushClientSecret = pushClientSecret,
            userPushStoreFactory = userPushStoreFactory,
        )
    }
}
