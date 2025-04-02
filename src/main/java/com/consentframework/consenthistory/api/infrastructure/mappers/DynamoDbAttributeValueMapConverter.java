package com.consentframework.consenthistory.api.infrastructure.mappers;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * DynamoDB attribute converter for AttributeValue maps.
 *
 * Required by the DynamoDB Enhanced Client to enable converting
 * between DynamoDB data and domain models.
 */
public class DynamoDbAttributeValueMapConverter implements AttributeConverter<Map<String, AttributeValue>> {

    /**
     * Convert from a Map&lt;String, AttributeValue&gt; to an AttributeValue.
     */
    @Override
    public AttributeValue transformFrom(final Map<String, AttributeValue> input) {
        return AttributeValue.fromM(input);
    }

    /**
     * Convert from an AttributeValue to a Map&lt;String, AttributeValue&gt;.
     */
    @Override
    public Map<String, AttributeValue> transformTo(final AttributeValue input) {
        return input.m();
    }

    /**
     * Return the attribute value type.
     */
    @Override
    public EnhancedType<Map<String, AttributeValue>> type() {
        return EnhancedType.mapOf(String.class, AttributeValue.class);
    }

    /**
     * Return the attribute value type.
     */
    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.M;
    }
}
