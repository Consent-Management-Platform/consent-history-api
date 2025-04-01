package com.consentframework.consenthistory.api.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.models.Consent;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.consenthistory.api.testcommon.utils.DynamoDbServiceUserConsentHistoryRecordGenerator;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

class DynamoDbConsentChangeEventMapperTest {
    @Test
    void toConsentChangeEventWhenNull() {
        final ConsentChangeEvent result = DynamoDbConsentChangeEventMapper.toConsentChangeEvent(null);
        assertNull(result);
    }

    @Test
    void toConsentWhenNoImages() {
        final DynamoDbServiceUserConsentHistoryRecord ddbRecord = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            null, null, ConsentEventType.MODIFY);
        validateConvertedConsentChangeEvent(ddbRecord);
    }

    @Test
    void toConsentForConsentInsertEvent() {
        final Map<String, AttributeValue> ddbOldImage = null;
        final Map<String, AttributeValue> ddbNewImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final DynamoDbServiceUserConsentHistoryRecord ddbRecord = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            ddbOldImage, ddbNewImage, ConsentEventType.INSERT);
        validateConvertedConsentChangeEvent(ddbRecord);
    }

    @Test
    void toConsentForConsentRemoveEvent() {
        final Map<String, AttributeValue> ddbOldImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final Map<String, AttributeValue> ddbNewImage = null;
        final DynamoDbServiceUserConsentHistoryRecord ddbRecord = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            ddbOldImage, ddbNewImage, ConsentEventType.REMOVE);
        validateConvertedConsentChangeEvent(ddbRecord);
    }

    @Test
    void toConsentForConsentModifyEvent() {
        final Map<String, AttributeValue> ddbOldImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final Map<String, AttributeValue> ddbNewImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("2");
        final DynamoDbServiceUserConsentHistoryRecord ddbRecord = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            ddbOldImage, ddbNewImage, ConsentEventType.MODIFY);
        validateConvertedConsentChangeEvent(ddbRecord);
    }

    @Test
    void toConsentForConsentUpdateEventDroppingAttributes() {
        final Map<String, AttributeValue> ddbOldImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final Map<String, AttributeValue> ddbNewImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage(
            "2", null, null);
        final DynamoDbServiceUserConsentHistoryRecord ddbRecord = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            ddbOldImage, ddbNewImage, ConsentEventType.MODIFY);

        final ConsentChangeEvent result = DynamoDbConsentChangeEventMapper.toConsentChangeEvent(ddbRecord);
        assertNotNull(result);
        assertEquals(ddbRecord.id(), result.getConsentId());
        assertEquals(ddbRecord.eventId(), result.getEventId());
        assertEquals(ddbRecord.eventTime(), result.getEventTime().toString());
        assertEquals(ddbRecord.eventType(), result.getEventType().name());
        validateConsentImage(ddbRecord.oldImage(), result.getOldImage(), TestConstants.TEST_CONSENT_DATA, TestConstants.TEST_EVENT_TIME);
        validateConsentImage(ddbRecord.newImage(), result.getNewImage(), null, null);
    }

    private void validateConvertedConsentChangeEvent(final DynamoDbServiceUserConsentHistoryRecord ddbRecord) {
        final ConsentChangeEvent result = DynamoDbConsentChangeEventMapper.toConsentChangeEvent(ddbRecord);
        assertNotNull(result);
        assertEquals(ddbRecord.id(), result.getConsentId());
        assertEquals(ddbRecord.eventId(), result.getEventId());
        assertEquals(ddbRecord.eventTime(), result.getEventTime().toString());
        assertEquals(ddbRecord.eventType(), result.getEventType().name());
        validateConsentImage(ddbRecord.oldImage(), result.getOldImage(), TestConstants.TEST_CONSENT_DATA, TestConstants.TEST_EVENT_TIME);
        validateConsentImage(ddbRecord.newImage(), result.getNewImage(), TestConstants.TEST_CONSENT_DATA, TestConstants.TEST_EVENT_TIME);
    }

    private void validateConsentImage(final Map<String, AttributeValue> ddbConsentImage, final Consent consent,
            final Map<String, String> expectedConsentData, final String expectedExpiryTime) {
        if (ddbConsentImage == null) {
            assertNull(consent);
            return;
        }
        assertNotNull(consent);
        assertEquals(TestConstants.TEST_SERVICE_ID, consent.getServiceId());
        assertEquals(TestConstants.TEST_USER_ID, consent.getUserId());
        assertEquals(TestConstants.TEST_CONSENT_ID, consent.getConsentId());
        assertEquals(Integer.valueOf(ddbConsentImage.get(Consent.JSON_PROPERTY_CONSENT_VERSION).n()), consent.getConsentVersion());
        assertEquals(ddbConsentImage.get(Consent.JSON_PROPERTY_STATUS).s(), consent.getStatus().name());
        assertEquals(ddbConsentImage.get(Consent.JSON_PROPERTY_CONSENT_TYPE).s(), consent.getConsentType());
        assertEquals(expectedConsentData, consent.getConsentData());
        assertEquals(expectedExpiryTime, expectedExpiryTime == null ? null : consent.getExpiryTime().toString());
    }
}
