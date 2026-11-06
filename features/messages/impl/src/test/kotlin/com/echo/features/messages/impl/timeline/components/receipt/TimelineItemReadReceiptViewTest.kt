/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.timeline.components.receipt

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.echo.features.messages.impl.timeline.model.ReadReceiptData
import com.echo.libraries.matrix.api.core.EventId
import com.echo.libraries.matrix.api.timeline.item.event.LocalEventSendState
import com.echo.libraries.matrix.test.A_USER_ID
import com.echo.libraries.matrix.test.room.aRoomMember
import com.echo.libraries.matrix.ui.messages.ProvideRoomMemberProfiles
import com.echo.libraries.testtags.TestTags
import com.echo.tests.testutils.robolectric.RobolectricTest
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class TimelineItemReadReceiptViewTest : RobolectricTest() {
    @Test
    fun `receipts are rendered when the preference is on`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent {
            ProvideRoomMemberProfiles(members = persistentListOf(aRoomMember(userId = A_USER_ID, displayName = "Alice"))) {
                TimelineItemReadReceiptView(
                    state = aReadReceiptViewState(renderReadReceipts = true),
                    onReadReceiptsClick = {},
                )
            }
        }
        onNodeWithTag(TestTags.messageReadReceipts.value).assertIsDisplayed()
    }

    @Test
    fun `receipts are not rendered when the preference is off`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent {
            ProvideRoomMemberProfiles(members = persistentListOf(aRoomMember(userId = A_USER_ID, displayName = "Alice"))) {
                TimelineItemReadReceiptView(
                    state = aReadReceiptViewState(renderReadReceipts = false),
                    onReadReceiptsClick = {},
                )
            }
        }
        onNodeWithTag(TestTags.messageReadReceipts.value).assertDoesNotExist()
    }

    private fun aReadReceiptViewState(renderReadReceipts: Boolean) = ReadReceiptViewState(
        sendState = LocalEventSendState.Sent(EventId("\$anEventId")),
        isLastOutgoingMessage = false,
        renderReadReceipts = renderReadReceipts,
        knownReceipts = kotlinx.collections.immutable.persistentListOf(
            ReadReceiptData(userId = A_USER_ID, formattedDate = "12:34"),
        ),
    )
}

