package com.consentframework.consenthistory.api.testcommon.utils;

import com.consentframework.consenthistory.api.infrastructure.repositories.InMemoryServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Utility class for generating test ConsentChangeEvent objects.
 */
public final class ConsentChangeEventGenerator {
    /**
     * Generates a ConsentChangeEvent with default values.
     */
    public static ConsentChangeEvent generate() {
        return generate(UUID.randomUUID().toString());
    }

    /**
     * Generates a ConsentChangeEvent with default values and a specific eventId.
     *
     * @param eventId the event ID
     */
    public static ConsentChangeEvent generate(final String eventId) {
        final String partitionKey = InMemoryServiceUserConsentHistoryRepository.getPartitionKey(
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        return new ConsentChangeEvent()
            .consentId(partitionKey)
            .eventId(eventId)
            .eventTime(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC))
            .eventType(ConsentEventType.MODIFY);
    }
}
