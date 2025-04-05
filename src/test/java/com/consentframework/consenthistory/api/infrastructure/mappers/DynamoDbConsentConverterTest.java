package com.consentframework.consenthistory.api.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consenthistory.api.JSON;
import com.consentframework.consenthistory.api.domain.entities.StoredConsent;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class DynamoDbConsentConverterTest {
    private DynamoDbConsentConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DynamoDbConsentConverter();
    }

    @Test
    void transformFromWhenNull() {
        final AttributeValue attributeValue = converter.transformFrom(null);
        assertNull(attributeValue);
    }

    @Test
    void transformFromWhenValid() throws Exception {
        final AttributeValue attributeValue = converter.transformFrom(TestConstants.TEST_STORED_CONSENT);
        assertNotNull(attributeValue);
        final String jsonString = attributeValue.s();
        assertNotNull(attributeValue);

        final StoredConsent consent = new JSON().getMapper().readValue(jsonString, StoredConsent.class);
        assertNotNull(consent);
        assertEquals(TestConstants.TEST_SERVICE_ID, consent.getServiceId());
        assertEquals(TestConstants.TEST_USER_ID, consent.getUserId());
        assertEquals(TestConstants.TEST_CONSENT_ID, consent.getConsentId());
        assertEquals(TestConstants.TEST_CONSENT_STATUS, consent.getConsentStatus());
        assertEquals(TestConstants.TEST_CONSENT_TYPE, consent.getConsentType());
        assertEquals(TestConstants.TEST_CONSENT_DATA, consent.getConsentData());
    }

    @Test
    void transformToWhenNull() {
        final StoredConsent consent = converter.transformTo(null);
        assertNull(consent);
    }

    @Test
    void transformToWhenEmpty() {
        final StoredConsent consent = converter.transformTo(AttributeValue.fromS(null));
        assertNull(consent);
    }

    @Test
    void transformToWhenInvalid() {
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            converter.transformTo(AttributeValue.fromS("{\"invalidKey\":1}"));
        });
        assertEquals("Error converting JSON string to StoredConsent", exception.getMessage());
    }

    @Test
    void transformToWhenValidConsent() throws Exception {
        final String consentJsonString = new JSON().getMapper().writeValueAsString(TestConstants.TEST_STORED_CONSENT);
        final StoredConsent consent = converter.transformTo(AttributeValue.fromS(consentJsonString));
        assertNotNull(consent);
        assertEquals(TestConstants.TEST_SERVICE_ID, consent.getServiceId());
        assertEquals(TestConstants.TEST_USER_ID, consent.getUserId());
        assertEquals(TestConstants.TEST_CONSENT_ID, consent.getConsentId());
        assertEquals(TestConstants.TEST_CONSENT_STATUS, consent.getConsentStatus());
        assertEquals(TestConstants.TEST_CONSENT_TYPE, consent.getConsentType());
        assertEquals(TestConstants.TEST_CONSENT_DATA, consent.getConsentData());
    }

    @Test
    void type() {
        final EnhancedType<StoredConsent> type = converter.type();
        assertNotNull(type);
        assertEquals(StoredConsent.class, type.rawClass());
    }

    @Test
    void attributeValueType() {
        assertEquals(AttributeValueType.S, converter.attributeValueType());
    }
}
