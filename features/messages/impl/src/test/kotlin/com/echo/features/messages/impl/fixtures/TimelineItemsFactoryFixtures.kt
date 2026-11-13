/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.messages.impl.fixtures

import com.echo.features.messages.impl.messagesummary.FakeMessageSummaryFormatter
import com.echo.features.messages.impl.timeline.factories.TimelineItemsFactory
import com.echo.features.messages.impl.timeline.factories.TimelineItemsFactoryConfig
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentFailedToParseMessageFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentFailedToParseStateFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentMessageFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentPollFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentProfileChangeFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentRedactedFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentRoomMembershipFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentStateFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentStickerFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemContentUTDFactory
import com.echo.features.messages.impl.timeline.factories.event.TimelineItemEventFactory
import com.echo.features.messages.impl.timeline.factories.virtual.TimelineItemDaySeparatorFactory
import com.echo.features.messages.impl.timeline.factories.virtual.TimelineItemVirtualFactory
import com.echo.features.messages.impl.timeline.groups.TimelineItemGrouper
import com.echo.features.messages.test.timeline.FakeHtmlConverterProvider
import com.echo.features.messages.test.timeline.FakeTextPillificator
import com.echo.features.poll.test.pollcontent.FakePollContentStateFactory
import com.echo.libraries.androidutils.filesize.FakeFileSizeFormatter
import com.echo.libraries.dateformatter.test.FakeDateFormatter
import com.echo.libraries.eventformatter.api.TimelineEventFormatter
import com.echo.libraries.featureflag.test.FakeFeatureFlagService
import com.echo.libraries.htmlrenderer.test.FakeHtmlMessageParser
import com.echo.libraries.matrix.api.core.UserId
import com.echo.libraries.matrix.api.timeline.item.event.EventContent
import com.echo.libraries.matrix.test.FakeMatrixClient
import com.echo.libraries.matrix.test.permalink.FakePermalinkParser
import com.echo.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import com.echo.services.toolbox.test.strings.FakeStringProvider
import com.echo.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope

internal fun TestScope.aTimelineItemsFactoryCreator(): TimelineItemsFactory.Creator {
    return object : TimelineItemsFactory.Creator {
        override fun create(config: TimelineItemsFactoryConfig): TimelineItemsFactory {
            return aTimelineItemsFactory(config)
        }
    }
}

internal fun aTimelineItemContentFactory(
    timelineEventFormatter: TimelineEventFormatter = aTimelineEventFormatter(),
    matrixClient: FakeMatrixClient = FakeMatrixClient(),
): TimelineItemContentFactory = TimelineItemContentFactory(
    messageFactory = TimelineItemContentMessageFactory(
        fileSizeFormatter = FakeFileSizeFormatter(),
        fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
        htmlConverterProvider = FakeHtmlConverterProvider(),
        permalinkParser = FakePermalinkParser(),
        textPillificator = FakeTextPillificator(),
        htmlMessageParser = FakeHtmlMessageParser(),
        featureFlagService = FakeFeatureFlagService(),
    ),
    redactedMessageFactory = TimelineItemContentRedactedFactory(),
    stickerFactory = TimelineItemContentStickerFactory(
        fileSizeFormatter = FakeFileSizeFormatter(),
        fileExtensionExtractor = FileExtensionExtractorWithoutValidation()
    ),
    pollFactory = TimelineItemContentPollFactory(FakePollContentStateFactory()),
    utdFactory = TimelineItemContentUTDFactory(),
    roomMembershipFactory = TimelineItemContentRoomMembershipFactory(timelineEventFormatter),
    profileChangeFactory = TimelineItemContentProfileChangeFactory(timelineEventFormatter),
    stateFactory = TimelineItemContentStateFactory(timelineEventFormatter),
    failedToParseMessageFactory = TimelineItemContentFailedToParseMessageFactory(),
    failedToParseStateFactory = TimelineItemContentFailedToParseStateFactory(),
    sessionId = matrixClient.sessionId,
    dateFormatter = FakeDateFormatter(),
    stringProvider = FakeStringProvider(),
)

internal fun TestScope.aTimelineItemsFactory(
    config: TimelineItemsFactoryConfig,
): TimelineItemsFactory {
    val matrixClient = FakeMatrixClient()
    return TimelineItemsFactory(
        dispatchers = testCoroutineDispatchers(),
        eventItemFactoryCreator = object : TimelineItemEventFactory.Creator {
            override fun create(config: TimelineItemsFactoryConfig): TimelineItemEventFactory {
                return TimelineItemEventFactory(
                    contentFactory = aTimelineItemContentFactory(
                        matrixClient = matrixClient,
                    ),
                    matrixClient = matrixClient,
                    dateFormatter = FakeDateFormatter(),
                    permalinkParser = FakePermalinkParser(),
                    config = config,
                    summaryFormatter = FakeMessageSummaryFormatter(),
                )
            }
        },
        virtualItemFactory = TimelineItemVirtualFactory(
            daySeparatorFactory = TimelineItemDaySeparatorFactory(
                FakeDateFormatter()
            ),
        ),
        timelineItemGrouper = TimelineItemGrouper(),
        config = config
    )
}

internal fun aTimelineEventFormatter(): TimelineEventFormatter {
    return object : TimelineEventFormatter {
        override fun format(content: EventContent, isOutgoing: Boolean, sender: UserId, senderDisambiguatedDisplayName: String): CharSequence? {
            return ""
        }
    }
}
