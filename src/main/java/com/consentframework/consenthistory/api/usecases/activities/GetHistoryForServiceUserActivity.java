package com.consentframework.consenthistory.api.usecases.activities;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentHistory;
import com.consentframework.consenthistory.api.models.GetHistoryForServiceUserResponseContent;
import com.consentframework.shared.api.domain.exceptions.InternalServiceException;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;

import java.util.List;

/**
 * GetHistoryForServiceUser API activity.
 */
public class GetHistoryForServiceUserActivity {
    private final ServiceUserConsentHistoryRepository consentHistoryRepository;

    /**
     * Constructor for GetHistoryForServiceUserConsentActivity.
     *
     * @param consentHistoryRepository the repository to access consent history data
     */
    public GetHistoryForServiceUserActivity(final ServiceUserConsentHistoryRepository consentHistoryRepository) {
        this.consentHistoryRepository = consentHistoryRepository;
    }

    /**
     * Handle request to retrieve history for a given ServiceUserConsent.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @return history for a service user if exists
     * @throws InternalServiceException exception thrown if unexpected error querying repository
     * @throws ResourceNotFoundException exception thrown if no consents exist for the service user
     */
    public GetHistoryForServiceUserResponseContent handleRequest(final String serviceId, final String userId)
            throws InternalServiceException, ResourceNotFoundException {
        final List<ConsentHistory> consentHistory = consentHistoryRepository.getServiceUserHistory(serviceId, userId);
        return new GetHistoryForServiceUserResponseContent().data(consentHistory);
    }
}
