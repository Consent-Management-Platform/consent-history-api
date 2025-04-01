package com.consentframework.consenthistory.api.infrastructure.mappers;

import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.models.Consent;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.models.ConsentStatus;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for converting between DynamoDB consent change event records and domain models.
 */
public final class DynamoDbConsentChangeEventMapper {
    /**
     * Converts a DynamoDB consent change event record to a domain model.
     *
     * @param record the DynamoDB record to convert.
     * @return the converted ConsentChangeEvent.
     */
    public static ConsentChangeEvent toConsentChangeEvent(final DynamoDbServiceUserConsentHistoryRecord record) {
        if (record == null) {
            return null;
        }
        return new ConsentChangeEvent()
            .consentId(record.id())
            .eventId(record.eventId())
            .eventTime(OffsetDateTime.parse(record.eventTime()).withOffsetSameLocal(ZoneOffset.UTC))
            .eventType(ConsentEventType.fromValue(record.eventType()))
            .oldImage(toConsent(record.oldImage()))
            .newImage(toConsent(record.newImage()));
    }

    private static Consent toConsent(final Map<String, AttributeValue> ddbConsent) {
        if (ddbConsent == null) {
            return null;
        }
        return new Consent()
            .serviceId(ddbConsent.get(Consent.JSON_PROPERTY_SERVICE_ID).s())
            .userId(ddbConsent.get(Consent.JSON_PROPERTY_USER_ID).s())
            .consentId(ddbConsent.get(Consent.JSON_PROPERTY_CONSENT_ID).s())
            .consentVersion(Integer.valueOf(ddbConsent.get(Consent.JSON_PROPERTY_CONSENT_VERSION).n()))
            .status(ConsentStatus.fromValue(ddbConsent.get(Consent.JSON_PROPERTY_STATUS).s()))
            .consentType(toString(ddbConsent.get(Consent.JSON_PROPERTY_CONSENT_TYPE)))
            .consentData(toStringMap(ddbConsent.get(Consent.JSON_PROPERTY_CONSENT_DATA)))
            .expiryTime(toOffsetDateTime(ddbConsent.get(Consent.JSON_PROPERTY_EXPIRY_TIME)));
    }

    private static String toString(final AttributeValue optionalAttributeValue) {
        if (optionalAttributeValue == null) {
            return null;
        }
        return optionalAttributeValue.s();
    }

    private static Map<String, String> toStringMap(final AttributeValue optionalAttributeValue) {
        if (optionalAttributeValue == null) {
            return null;
        }
        return optionalAttributeValue.m().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().s()));
    }

    private static OffsetDateTime toOffsetDateTime(final AttributeValue optionalAttributeValue) {
        if (optionalAttributeValue == null) {
            return null;
        }
        return OffsetDateTime.parse(optionalAttributeValue.s()).withOffsetSameLocal(ZoneOffset.UTC);
    }
}
