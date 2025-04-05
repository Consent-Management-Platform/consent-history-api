package com.consentframework.consenthistory.api.infrastructure.mappers;

import com.consentframework.consenthistory.api.JSON;
import com.consentframework.consenthistory.api.domain.entities.StoredConsent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Convert between StoredConsent objects and DynamoDB JSON string attribute values.
 */
public class DynamoDbConsentConverter implements AttributeConverter<StoredConsent> {
    private static final Logger logger = LogManager.getLogger(DynamoDbConsentConverter.class);
    private final ObjectMapper objectMapper;

    public DynamoDbConsentConverter() {
        this.objectMapper = new JSON().getMapper();
    }

    /**
     * Convert from a StoredConsent object to an AttributeValue that can be stored in a DynamoDB record.
     */
    @Override
    public AttributeValue transformFrom(final StoredConsent consent) {
        if (consent == null) {
            return null;
        }

        final String consentJsonString;
        try {
            consentJsonString = this.objectMapper.writeValueAsString(consent);
        } catch (final JsonProcessingException e) {
            logger.error("Error converting StoredConsent to JSON string: {}", e.getMessage(), e);
            throw new RuntimeException("Error converting StoredConsent to JSON string", e);
        }
        return AttributeValue.fromS(consentJsonString);
    }

    /**
     * Convert from a DynamoDB JSON string attribute value to a StoredConsent object.
     */
    @Override
    public StoredConsent transformTo(final AttributeValue input) {
        if (input == null || input.s() == null) {
            return null;
        }
        final String jsonString = input.s();
        try {
            return this.objectMapper.readValue(jsonString, StoredConsent.class);
        } catch (final JsonProcessingException e) {
            logger.error("Error converting JSON string to StoredConsent: {}", e.getMessage(), e);
            throw new RuntimeException("Error converting JSON string to StoredConsent", e);
        }
    }

    /**
     * Return the EnhancedType for StoredConsent objects.
     */
    @Override
    public EnhancedType<StoredConsent> type() {
        return EnhancedType.of(StoredConsent.class);
    }

    /**
     * Return the DynamoDB attribute value type.
     */
    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
