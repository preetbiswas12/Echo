/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.matrix.test.encryption

import com.echo.libraries.matrix.api.encryption.PasswordStrengthEstimate
import com.echo.libraries.matrix.api.encryption.PasswordStrengthEstimator
import com.echo.libraries.matrix.api.encryption.PasswordStrengthRanking

class FakePasswordStrengthEstimator(
    private val estimateResult: (password: String, userInputs: List<String>) -> PasswordStrengthEstimate =
        { _, _ -> aPasswordStrengthEstimate() },
) : PasswordStrengthEstimator {
    override fun estimate(password: String, userInputs: List<String>): PasswordStrengthEstimate =
        estimateResult(password, userInputs)
}

fun aPasswordStrengthEstimate(
    ranking: PasswordStrengthRanking = PasswordStrengthRanking.Strong,
    score: Double = 18.0,
    normalScore: Double = 0.7,
) = PasswordStrengthEstimate(
    ranking = ranking,
    score = score,
    normalScore = normalScore,
)

