package com.consentframework.consenthistory.api.testcommon.utils;

import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Utility class for generating test DynamoDbServiceUserConsentHistoryRecord objects.
 */
public final class DynamoDbServiceUserConsentHistoryRecordGenerator {
    /**
     * Generates a DynamoDbServiceUserConsentHistoryRecord with default values.
     */
    public static DynamoDbServiceUserConsentHistoryRecord generate() {
        return DynamoDbServiceUserConsentHistoryRecord.builder()
            .id(TestConstants.TEST_PARTITION_KEY)
            .eventId(UUID.randomUUID().toString())
            .eventType(ConsentEventType.MODIFY.name())
            .eventTime(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toString())
            .build();
    }
}
