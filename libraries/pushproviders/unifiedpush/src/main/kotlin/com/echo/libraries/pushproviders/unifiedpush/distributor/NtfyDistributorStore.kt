/*
 * Copyright (c) 2026 Echo contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.pushproviders.unifiedpush.distributor

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A registration from an application to the embedded distributor: the application [token]
 * from the UnifiedPush protocol is mapped to an ntfy [topic].
 */
@Serializable
data class NtfyRegistration(
    val packageName: String,
    val token: String,
    val topic: String,
)

/**
 * Store of the [NtfyRegistration]s of the embedded distributor, backed by SharedPreferences.
 */
internal class NtfyDistributorStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun registrations(): List<NtfyRegistration> {
        return preferences.getString(KEY_REGISTRATIONS, null)
            ?.let { encoded -> runCatching { json.decodeFromString<List<NtfyRegistration>>(encoded) }.getOrNull() }
            .orEmpty()
    }

    /**
     * Register [packageName] with [token], returning the registration to use.
     * If the token is already registered, the existing registration (and so the existing topic)
     * is returned, the UnifiedPush spec requires not to create a new endpoint in this case.
     */
    fun register(packageName: String, token: String): NtfyRegistration {
        val registrations = registrations().toMutableList()
        val existing = registrations.firstOrNull { it.token == token }
        val registration = existing
            ?: NtfyRegistration(packageName, token, NtfyTopic.generate()).also { registrations.add(it) }
        save(registrations)
        return registration
    }

    fun remove(token: String): NtfyRegistration? {
        val registrations = registrations().toMutableList()
        val removed = registrations.firstOrNull { it.token == token } ?: return null
        registrations.removeAll { it.token == token }
        save(registrations)
        return removed
    }

    fun getForTopic(topic: String): NtfyRegistration? {
        return registrations().firstOrNull { it.topic == topic }
    }

    private fun save(registrations: List<NtfyRegistration>) {
        preferences.edit {
            putString(KEY_REGISTRATIONS, json.encodeToString(registrations))
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "unifiedpush_distributor_store"
        const val KEY_REGISTRATIONS = "registrations"
    }
}
