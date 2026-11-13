/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.htmlrenderer.test

import com.echo.libraries.htmlrenderer.api.DocumentNode
import com.echo.libraries.htmlrenderer.api.HtmlMessageParser
import com.echo.tests.testutils.lambda.lambdaError
import org.jsoup.nodes.Document

class FakeHtmlMessageParser(
    private val parseResult: (Document) -> DocumentNode = { lambdaError() },
) : HtmlMessageParser {
    override fun parse(document: Document): DocumentNode = parseResult(document)
}
