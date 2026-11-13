/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.slashcommands.test

import com.echo.libraries.matrix.api.timeline.Timeline
import com.echo.libraries.slashcommands.api.SlashCommand
import com.echo.libraries.slashcommands.api.SlashCommandService
import com.echo.libraries.slashcommands.api.SlashCommandSuggestion
import com.echo.tests.testutils.lambda.lambdaError
import com.echo.tests.testutils.simulateLongTask

class FakeSlashCommandService(
    private val getSuggestionsResult: (String, Boolean) -> List<SlashCommandSuggestion> = { _, _ -> lambdaError() },
    private val parseResult: (CharSequence, String?, Boolean) -> SlashCommand = { _, _, _ -> lambdaError() },
    private val proceedSendMessageResult: (SlashCommand.SlashCommandSendMessage, Timeline) -> Result<Unit> = { _, _ -> lambdaError() },
    private val proceedAdminResult: (SlashCommand.SlashCommandAdmin) -> Result<Unit> = { lambdaError() },
) : SlashCommandService {
    override suspend fun getSuggestions(text: String, isInThread: Boolean): List<SlashCommandSuggestion> = simulateLongTask {
        getSuggestionsResult(text, isInThread)
    }

    override suspend fun parse(
        textMessage: CharSequence,
        formattedMessage: String?,
        isInThreadTimeline: Boolean,
    ): SlashCommand = simulateLongTask {
        parseResult(textMessage, formattedMessage, isInThreadTimeline)
    }

    override suspend fun proceedSendMessage(
        slashCommand: SlashCommand.SlashCommandSendMessage,
        timeline: Timeline,
    ): Result<Unit> = simulateLongTask {
        proceedSendMessageResult(slashCommand, timeline)
    }

    override suspend fun proceedAdmin(slashCommand: SlashCommand.SlashCommandAdmin): Result<Unit> = simulateLongTask {
        proceedAdminResult(slashCommand)
    }
}
