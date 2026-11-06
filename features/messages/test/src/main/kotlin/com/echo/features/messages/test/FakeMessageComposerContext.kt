/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.test

import com.echo.features.messages.api.MessageComposerContext
import com.echo.libraries.textcomposer.model.MessageComposerMode

class FakeMessageComposerContext(
    override var composerMode: MessageComposerMode = MessageComposerMode.Normal
) : MessageComposerContext

