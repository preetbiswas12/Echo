/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush.distributor

/**
 * Configuration of the UnifiedPush distributor embedded in Echo.
 *
 * Echo acts as its own UnifiedPush distributor: it subscribes to the ntfy server below with a
 * WebSocket ([NtfyPushService]) and delivers the messages it receives to the app itself through
 * the UnifiedPush Android protocol, see https://unifiedpush.org/developers/spec/android/.
 */
object DistributorConfig {
    /**
     * The ntfy server Echo subscribes to for push. It is baked in: there is no configuration
     * knob for it. The server must expose the Matrix push gateway, i.e. respond to
     * `GET /_matrix/push/v1/notify` with `{"unifiedpush":{"gateway":"matrix"}}` and accept the
     * Matrix notifications POSTed on the same path, ntfy.sh does.
     */
    const val NTFY_SERVER_BASE_URL: String = "https://ntfy.sh"
}
