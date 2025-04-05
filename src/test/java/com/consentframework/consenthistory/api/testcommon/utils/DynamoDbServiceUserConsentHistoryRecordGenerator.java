package com.consentframework.consenthistory.api.testcommon.utils;

import com.consentframework.consenthistory.api.domain.entities.StoredConsent;
import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.models.ConsentStatus;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for generating test DynamoDbServiceUserConsentHistoryRecord objects.
 */
public final class DynamoDbServiceUserConsentHistoryRecordGenerator {
    /**
     * Generates a DynamoDbServiceUserConsentHistoryRecord with default values.
     */
    public static DynamoDbServiceUserConsentHistoryRecord generate() {
        return generate(
            generateDdbConsentImage("1"),
            generateDdbConsentImage("2"),
            ConsentEventType.MODIFY
        );
    }

    /**
     * Generates a DynamoDbServiceUserConsentHistoryRecord with default values.
     *
     * @param oldImage old consent before change event
     * @param newImage new consent after change event
     * @param eventType type of change
     */
    public static DynamoDbServiceUserConsentHistoryRecord generate(
            final StoredConsent oldImage,
            final StoredConsent newImage,
            final ConsentEventType eventType) {
        return DynamoDbServiceUserConsentHistoryRecord.builder()
            .id(TestConstants.TEST_PARTITION_KEY)
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType.name())
            .eventTime(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toString())
            .oldImage(oldImage)
            .newImage(newImage)
            .build();
    }

    /**
     * Generates a consent image with default values.
     *
     * @param consentVersion version
     */
    public static StoredConsent generateDdbConsentImage(final String consentVersion) {
        return generateDdbConsentImage(consentVersion, TestConstants.TEST_CONSENT_DATA, TestConstants.TEST_EVENT_TIME);
    }

    /**
     * Generates a consent image with default values.
     *
     * @param consentVersion version
     * @param consentData nested consent data
     */
    public static StoredConsent generateDdbConsentImage(final String consentVersion,
            final Map<String, String> consentData, final String expiryTime) {
        final StoredConsent consent = new StoredConsent()
            .serviceId(TestConstants.TEST_SERVICE_ID)
            .userId(TestConstants.TEST_USER_ID)
            .consentId(TestConstants.TEST_CONSENT_ID)
            .consentVersion(Integer.valueOf(consentVersion))
            .consentStatus(ConsentStatus.ACTIVE)
            .consentType(TestConstants.TEST_CONSENT_TYPE)
            .consentData(consentData);

        if (expiryTime != null) {
            consent.setExpiryTime(OffsetDateTime.parse(expiryTime).withOffsetSameInstant(ZoneOffset.UTC));
        }
        return consent;
    }
}
