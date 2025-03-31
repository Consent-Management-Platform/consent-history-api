package com.consentframework.consenthistory.api.infrastructure.repositories;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;

import java.util.List;

/**
 * DynamoDB implementation of the ServiceUserConsentHistory repository.
 */
public class DynamoDbServiceUserConsentHistoryRepository implements ServiceUserConsentHistoryRepository {
    /**
     * Retrieve history for a given service user consent.
     */
    @Override
    public List<ConsentChangeEvent> getConsentHistory(String serviceId, String userId, String consentId)
            throws ResourceNotFoundException {
        // TODO: Implement logic to retrieve consent history from DynamoDB
        final String notFoundMessage = String.format(CONSENT_NOT_FOUND_MESSAGE,
            serviceId, userId, consentId);
        throw new ResourceNotFoundException(notFoundMessage);
    }
}
