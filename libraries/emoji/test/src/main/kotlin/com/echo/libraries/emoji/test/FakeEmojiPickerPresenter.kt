/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.libraries.emoji.test

import com.echo.libraries.emoji.api.picker.EmojiPickerPresenter
import com.echo.libraries.emoji.api.picker.EmojiPickerState

fun fakeEmojiPickerPresenter(
    state: EmojiPickerState = FakeEmojiPickerState(isReady = true),
): EmojiPickerPresenter = EmojiPickerPresenter { state }

fun fakeEmojiPickerPresenterFactory(
    presenter: EmojiPickerPresenter = fakeEmojiPickerPresenter(),
): EmojiPickerPresenter.Factory = EmojiPickerPresenter.Factory { presenter }

