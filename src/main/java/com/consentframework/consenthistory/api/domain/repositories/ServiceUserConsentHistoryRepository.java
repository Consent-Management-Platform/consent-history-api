package com.consentframework.consenthistory.api.domain.repositories;

import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentHistory;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;

import java.util.List;

/**
 * Defines supported integrations with service user consent history records.
 */
public interface ServiceUserConsentHistoryRepository {
    public static final String CONSENT_NOT_FOUND_MESSAGE = "No consent history found for serviceId: %s, userId: %s, consentId: %s";
    public static final String SERVICE_USER_CONSENTS_NOT_FOUND = "No consent history found for serviceId: %s, userId: %s";

    /**
     * Retrieve history for a given service user consent.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     * @return list of consent change events for the consent
     * @throws ResourceNotFoundException if no history is found for the consent
     */
    List<ConsentChangeEvent> getConsentHistory(final String serviceId, final String userId, final String consentId)
        throws ResourceNotFoundException;

    /**
     * Retrieve history for a given service user.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @return list of consent histories for the service user, each having a consent ID and its change events
     * @throws ResourceNotFoundException if no history is found for the service user
     */
    List<ConsentHistory> getServiceUserHistory(final String serviceId, final String userId)
        throws ResourceNotFoundException;
}
