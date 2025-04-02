package com.consentframework.consenthistory.api.infrastructure.repositories;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of ServiceUserConsentHistoryRepository used for testing.
 */
public class InMemoryServiceUserConsentHistoryRepository implements ServiceUserConsentHistoryRepository {
    private Map<String, List<ConsentChangeEvent>> consentHistoryStore;

    /**
     * InMemoryServiceUserConsentHistoryRepository constructor.
     */
    public InMemoryServiceUserConsentHistoryRepository() {
        this.consentHistoryStore = new HashMap<>();
    }

    /**
     * Retrieve history for a given service user consent.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     */
    @Override
    public List<ConsentChangeEvent> getConsentHistory(String serviceId, String userId, String consentId)
            throws ResourceNotFoundException {
        final String partitionKey = getPartitionKey(serviceId, userId, consentId);
        final List<ConsentChangeEvent> consentHistory = consentHistoryStore.get(partitionKey);
        if (consentHistory == null || consentHistory.isEmpty()) {
            final String errorMessage = String.format(ServiceUserConsentHistoryRepository.CONSENT_NOT_FOUND_MESSAGE,
                serviceId, userId, consentId);
            throw new ResourceNotFoundException(errorMessage);
        }
        return consentHistory;
    }

    /**
     * Add a consent history record for a given service user consent.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     * @param consentHistoryRecord consent history record
     */
    public void addConsentHistoryRecord(final String serviceId, final String userId, final String consentId,
            final ConsentChangeEvent consentHistoryRecord) {
        final String partitionKey = getPartitionKey(serviceId, userId, consentId);

        List<ConsentChangeEvent> consentHistory = consentHistoryStore.get(partitionKey);
        if (consentHistory == null) {
            consentHistory = new ArrayList<>();
        }
        consentHistory.add(consentHistoryRecord);
        consentHistoryStore.put(partitionKey, consentHistory);
    }

    /**
     * Generate a partition key for the given service ID, user ID, and consent ID.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     */
    public static String getPartitionKey(String serviceId, String userId, String consentId) {
        return String.format("%s|%s|%s", serviceId, userId, consentId);
    }
}
