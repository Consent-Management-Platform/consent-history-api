package com.consentframework.consenthistory.api.infrastructure.mappers;

import com.consentframework.consenthistory.api.models.Consent;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.models.ConsentStatus;
import com.consentframework.shared.api.infrastructure.entities.DynamoDbConsentHistory;
import com.consentframework.shared.api.infrastructure.entities.StoredConsentImage;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
    public static ConsentChangeEvent toConsentChangeEvent(final DynamoDbConsentHistory record) {
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

    private static Consent toConsent(final StoredConsentImage storedConsent) {
        if (storedConsent == null) {
            return null;
        }
        return new Consent()
            .consentId(storedConsent.getConsentId())
            .consentVersion(storedConsent.getConsentVersion())
            .userId(storedConsent.getUserId())
            .serviceId(storedConsent.getServiceId())
            .status(ConsentStatus.fromValue(storedConsent.getConsentStatus()))
            .consentType(storedConsent.getConsentType())
            .consentData(storedConsent.getConsentData())
            .expiryTime(storedConsent.getExpiryTime());
    }
}
