package com.consentframework.consenthistory.api.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.consentframework.consenthistory.api.domain.entities.StoredConsent;
import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.models.Consent;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.consenthistory.api.testcommon.utils.DynamoDbServiceUserConsentHistoryRecordGenerator;
import org.junit.jupiter.api.Test;

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
        final StoredConsent ddbOldImage = null;
        final StoredConsent ddbNewImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final DynamoDbServiceUserConsentHistoryRecord ddbRecord = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            ddbOldImage, ddbNewImage, ConsentEventType.INSERT);
        validateConvertedConsentChangeEvent(ddbRecord);
    }

    @Test
    void toConsentForConsentRemoveEvent() {
        final StoredConsent ddbOldImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final StoredConsent ddbNewImage = null;
        final DynamoDbServiceUserConsentHistoryRecord ddbRecord = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            ddbOldImage, ddbNewImage, ConsentEventType.REMOVE);
        validateConvertedConsentChangeEvent(ddbRecord);
    }

    @Test
    void toConsentForConsentModifyEvent() {
        final StoredConsent ddbOldImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final StoredConsent ddbNewImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("2");
        final DynamoDbServiceUserConsentHistoryRecord ddbRecord = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            ddbOldImage, ddbNewImage, ConsentEventType.MODIFY);
        validateConvertedConsentChangeEvent(ddbRecord);
    }

    @Test
    void toConsentForConsentUpdateEventDroppingAttributes() {
        final StoredConsent ddbOldImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final StoredConsent ddbNewImage = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage(
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

    private void validateConsentImage(final StoredConsent sourceDdbConsentImage, final Consent parsedConsent,
            final Map<String, String> expectedConsentData, final String expectedExpiryTime) {
        if (sourceDdbConsentImage == null) {
            assertNull(parsedConsent);
            return;
        }
        assertNotNull(parsedConsent);
        assertEquals(TestConstants.TEST_SERVICE_ID, parsedConsent.getServiceId());
        assertEquals(TestConstants.TEST_USER_ID, parsedConsent.getUserId());
        assertEquals(TestConstants.TEST_CONSENT_ID, parsedConsent.getConsentId());
        assertEquals(sourceDdbConsentImage.getConsentVersion(), parsedConsent.getConsentVersion());
        assertEquals(sourceDdbConsentImage.getConsentStatus(), parsedConsent.getStatus());
        assertEquals(sourceDdbConsentImage.getConsentType(), parsedConsent.getConsentType());
        assertEquals(expectedConsentData, parsedConsent.getConsentData());
        assertEquals(expectedExpiryTime, expectedExpiryTime == null ? null : parsedConsent.getExpiryTime().toString());
    }
}
