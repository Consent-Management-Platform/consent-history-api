package com.consentframework.consenthistory.api.usecases.activities;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.GetHistoryForServiceUserConsentResponseContent;
import com.consentframework.shared.api.domain.exceptions.InternalServiceException;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;

import java.util.List;

/**
 * GetHistoryForServiceUserConsent API activity.
 */
public class GetHistoryForServiceUserConsentActivity {
    private final ServiceUserConsentHistoryRepository consentHistoryRepository;

    /**
     * Constructor for GetHistoryForServiceUserConsentActivity.
     *
     * @param consentHistoryRepository the repository to access consent history data
     */
    public GetHistoryForServiceUserConsentActivity(ServiceUserConsentHistoryRepository consentHistoryRepository) {
        this.consentHistoryRepository = consentHistoryRepository;
    }

    /**
     * Handle request to retrieve history for a given ServiceUserConsent.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     * @return history for a service-user-consent ID tuple if exists
     * @throws InternalServiceException exception thrown if unexpected error querying repository
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    public GetHistoryForServiceUserConsentResponseContent handleRequest(final String serviceId, final String userId, final String consentId)
            throws InternalServiceException, ResourceNotFoundException {
        final List<ConsentChangeEvent> consentHistory = consentHistoryRepository.getConsentHistory(serviceId, userId, consentId);
        return new GetHistoryForServiceUserConsentResponseContent().data(consentHistory);
    }
}
