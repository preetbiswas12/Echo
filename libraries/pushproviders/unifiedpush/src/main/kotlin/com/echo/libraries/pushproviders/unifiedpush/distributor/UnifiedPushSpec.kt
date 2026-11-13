/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush.distributor

/**
 * Actions and extras of the UnifiedPush Android specification, as implemented by the
 * `org.unifiedpush.android:connector` library on the client side.
 *
 * The constants below are duplicated here because the ones in the connector library are
 * `internal`. They MUST match the specification:
 * https://unifiedpush.org/developers/spec/android/
 */
internal object UnifiedPushSpec {
    // Actions received by the distributor.
    const val ACTION_REGISTER = "org.unifiedpush.android.distributor.REGISTER"
    const val ACTION_UNREGISTER = "org.unifiedpush.android.distributor.UNREGISTER"
    const val ACTION_MESSAGE_ACK = "org.unifiedpush.android.distributor.MESSAGE_ACK"

    // Actions sent by the distributor to the end user application.
    const val ACTION_NEW_ENDPOINT = "org.unifiedpush.android.connector.NEW_ENDPOINT"
    const val ACTION_UNREGISTERED = "org.unifiedpush.android.connector.UNREGISTERED"
    const val ACTION_MESSAGE = "org.unifiedpush.android.connector.MESSAGE"

    // Extras.
    const val EXTRA_TOKEN = "token"
    const val EXTRA_ENDPOINT = "endpoint"
    const val EXTRA_BYTES_MESSAGE = "bytesMessage"
    const val EXTRA_MESSAGE_ID = "id"
    const val EXTRA_PENDING_INTENT = "pi"
}
