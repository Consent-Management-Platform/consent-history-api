package com.consentframework.consenthistory.api.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

class DynamoDbAttributeValueMapConverterTest {
    private DynamoDbAttributeValueMapConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DynamoDbAttributeValueMapConverter();
    }

    @Test
    void testConvertToAttributeValue() {
        final Map<String, AttributeValue> attributeValueMap = TestConstants.TEST_DDB_CONSENT_DATA;
        final AttributeValue result = converter.transformFrom(attributeValueMap);
        assertEquals(attributeValueMap, result.m());
    }

    @Test
    void testConvertToAttributeValueMap() {
        final AttributeValue attributeValue = AttributeValue.fromM(TestConstants.TEST_DDB_CONSENT_DATA);
        final Map<String, AttributeValue> result = converter.transformTo(attributeValue);
        assertEquals(TestConstants.TEST_DDB_CONSENT_DATA, result);
    }
}
