/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.lockscreen.impl.fixtures

import com.echo.features.lockscreen.impl.pin.DefaultPinCodeManager
import com.echo.features.lockscreen.impl.pin.PinCodeManager
import com.echo.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import com.echo.features.lockscreen.impl.storage.LockScreenStore
import com.echo.libraries.cryptography.api.EncryptionDecryptionService
import com.echo.libraries.cryptography.impl.AESEncryptionDecryptionService
import com.echo.libraries.cryptography.test.SimpleSecretKeyRepository

internal fun aPinCodeManager(
    lockScreenStore: LockScreenStore = InMemoryLockScreenStore(),
    secretKeyRepository: SimpleSecretKeyRepository = SimpleSecretKeyRepository(),
    encryptionDecryptionService: EncryptionDecryptionService = AESEncryptionDecryptionService(),
): PinCodeManager {
    return DefaultPinCodeManager(secretKeyRepository, encryptionDecryptionService, lockScreenStore)
}
