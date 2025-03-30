package com.consentframework.consenthistory.api.domain.repositories;

import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;

import java.util.List;

/**
 * Defines supported integrations with service user consent history records.
 */
public interface ServiceUserConsentHistoryRepository {
    /**
     * Retrieve history for a given service user consent.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     */
    List<ConsentChangeEvent> getConsentHistory(final String serviceId, final String userId, final String consentId)
        throws ResourceNotFoundException;
}
