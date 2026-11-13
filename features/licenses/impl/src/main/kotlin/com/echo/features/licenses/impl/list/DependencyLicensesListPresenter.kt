/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.licenses.impl.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.echo.features.licenses.impl.LicensesProvider
import com.echo.features.licenses.impl.model.DependencyLicenseItem
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.architecture.Presenter
import com.echo.libraries.core.extensions.runCatchingExceptions
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Inject
class DependencyLicensesListPresenter(
    private val licensesProvider: LicensesProvider,
) : Presenter<DependencyLicensesListState> {
    @Composable
    override fun present(): DependencyLicensesListState {
        var licenses by remember {
            mutableStateOf<AsyncData<ImmutableList<DependencyLicenseItem>>>(AsyncData.Loading())
        }
        var filteredLicenses by remember {
            mutableStateOf<AsyncData<ImmutableList<DependencyLicenseItem>>>(AsyncData.Loading())
        }
        var filter by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            runCatchingExceptions {
                licenses = AsyncData.Success(licensesProvider.provides().toImmutableList())
            }.onFailure {
                licenses = AsyncData.Failure(it)
            }
        }
        LaunchedEffect(filter, licenses.dataOrNull()) {
            val data = licenses.dataOrNull()
            val safeFilter = filter.trim()
            if (data != null && safeFilter.isNotEmpty()) {
                filteredLicenses = AsyncData.Success(data.filter {
                    it.safeName.contains(safeFilter, ignoreCase = true) ||
                        it.groupId.contains(safeFilter, ignoreCase = true) ||
                        it.artifactId.contains(safeFilter, ignoreCase = true)
                }.toImmutableList())
            } else {
                filteredLicenses = licenses
            }
        }

        fun handleEvent(event: DependencyLicensesListEvent) {
            when (event) {
                is DependencyLicensesListEvent.SetFilter -> {
                    filter = event.filter
                }
            }
        }

        return DependencyLicensesListState(
            licenses = filteredLicenses,
            filter = filter,
            eventSink = ::handleEvent,
        )
    }
}
