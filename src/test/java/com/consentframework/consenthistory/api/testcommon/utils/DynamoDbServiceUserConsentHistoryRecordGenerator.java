package com.consentframework.consenthistory.api.testcommon.utils;

import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.models.ConsentStatus;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.shared.api.infrastructure.entities.DynamoDbConsentHistory;
import com.consentframework.shared.api.infrastructure.entities.StoredConsentImage;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for generating test DynamoDbConsentHistory objects.
 */
public final class DynamoDbServiceUserConsentHistoryRecordGenerator {
    /**
     * Generates a DynamoDbConsentHistory with default values.
     */
    public static DynamoDbConsentHistory generate() {
        return generate(
            generateDdbConsentImage("1"),
            generateDdbConsentImage("2"),
            ConsentEventType.MODIFY
        );
    }

    /**
     * Generates a DynamoDbConsentHistory with default values.
     *
     * @param oldImage old consent before change event
     * @param newImage new consent after change event
     * @param eventType type of change
     */
    public static DynamoDbConsentHistory generate(
            final StoredConsentImage oldImage,
            final StoredConsentImage newImage,
            final ConsentEventType eventType) {
        return generate(
            oldImage,
            newImage,
            eventType,
            OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC)
        );
    }

    /**
     * Generates a DynamoDbConsentHistory with default values.
     *
     * @param oldImage old consent before change event
     * @param newImage new consent after change event
     * @param eventType type of change
     * @param eventTime time of change event
     */
    public static DynamoDbConsentHistory generate(
            final StoredConsentImage oldImage,
            final StoredConsentImage newImage,
            final ConsentEventType eventType,
            final OffsetDateTime eventTime) {
        return DynamoDbConsentHistory.builder()
            .id(TestConstants.TEST_PARTITION_KEY)
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType.name())
            .eventTime(eventTime.toString())
            .serviceUserId(TestConstants.TEST_SERVICE_USER_ID)
            .oldImage(oldImage)
            .newImage(newImage)
            .build();
    }

    /**
     * Generates a consent image with default values.
     *
     * @param consentVersion version
     */
    public static StoredConsentImage generateDdbConsentImage(final String consentVersion) {
        return generateDdbConsentImage(consentVersion, TestConstants.TEST_CONSENT_DATA, TestConstants.TEST_EVENT_TIME);
    }

    /**
     * Generates a consent image with default values.
     *
     * @param consentVersion version
     * @param consentData nested consent data
     */
    public static StoredConsentImage generateDdbConsentImage(final String consentVersion,
            final Map<String, String> consentData, final String expiryTime) {
        final StoredConsentImage consent = new StoredConsentImage()
            .serviceId(TestConstants.TEST_SERVICE_ID)
            .userId(TestConstants.TEST_USER_ID)
            .consentId(TestConstants.TEST_CONSENT_ID)
            .consentVersion(Integer.valueOf(consentVersion))
            .consentStatus(ConsentStatus.ACTIVE.getValue())
            .consentType(TestConstants.TEST_CONSENT_TYPE)
            .consentData(consentData);

        if (expiryTime != null) {
            consent.setExpiryTime(OffsetDateTime.parse(expiryTime).withOffsetSameInstant(ZoneOffset.UTC));
        }
        return consent;
    }
}
