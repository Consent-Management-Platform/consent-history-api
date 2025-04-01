package com.consentframework.consenthistory.api.testcommon.utils;

import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.models.Consent;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.models.ConsentStatus;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
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
            final Map<String, AttributeValue> oldImage,
            final Map<String, AttributeValue> newImage,
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
    public static Map<String, AttributeValue> generateDdbConsentImage(final String consentVersion) {
        return generateDdbConsentImage(consentVersion, TestConstants.TEST_DDB_CONSENT_DATA, TestConstants.TEST_EVENT_TIME);
    }

    /**
     * Generates a consent image with default values.
     *
     * @param consentVersion version
     * @param consentData nested consent data
     */
    public static Map<String, AttributeValue> generateDdbConsentImage(final String consentVersion,
            final Map<String, AttributeValue> consentData, final String expiryTime) {
        final Map<String, AttributeValue> ddbConsentImage = new HashMap<>(Map.of(
            Consent.JSON_PROPERTY_SERVICE_ID, AttributeValue.builder().s(TestConstants.TEST_SERVICE_ID).build(),
            Consent.JSON_PROPERTY_USER_ID, AttributeValue.builder().s(TestConstants.TEST_USER_ID).build(),
            Consent.JSON_PROPERTY_CONSENT_ID, AttributeValue.builder().s(TestConstants.TEST_CONSENT_ID).build(),
            Consent.JSON_PROPERTY_CONSENT_VERSION, AttributeValue.builder().n(consentVersion).build(),
            Consent.JSON_PROPERTY_STATUS, AttributeValue.builder().s(ConsentStatus.ACTIVE.name()).build(),
            Consent.JSON_PROPERTY_CONSENT_TYPE, AttributeValue.builder().s(TestConstants.TEST_CONSENT_TYPE).build()
        ));
        if (consentData != null) {
            ddbConsentImage.put(Consent.JSON_PROPERTY_CONSENT_DATA, AttributeValue.builder().m(consentData).build());
        }
        if (expiryTime != null) {
            ddbConsentImage.put(Consent.JSON_PROPERTY_EXPIRY_TIME, AttributeValue.builder().s(expiryTime).build());
        }
        return ddbConsentImage;
    }
}
