package com.consentframework.consenthistory.api.infrastructure.repositories;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentHistory;
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
    private Map<String, List<ConsentHistory>> consentHistoryByServiceUserStore;

    /**
     * InMemoryServiceUserConsentHistoryRepository constructor.
     */
    public InMemoryServiceUserConsentHistoryRepository() {
        this.consentHistoryStore = new HashMap<>();
        this.consentHistoryByServiceUserStore = new HashMap<>();
    }

    /**
     * Retrieve history for a given service user consent.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     */
    @Override
    public List<ConsentChangeEvent> getConsentHistory(final String serviceId, final String userId, final String consentId)
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
     * Retrieve history for a given service user.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     */
    @Override
    public List<ConsentHistory> getServiceUserHistory(final String serviceId, final String userId) throws ResourceNotFoundException {
        final String serviceUserId = getServiceUserId(serviceId, userId);
        final List<ConsentHistory> consentHistories = consentHistoryByServiceUserStore.get(serviceUserId);

        if (consentHistories == null || consentHistories.isEmpty()) {
            final String errorMessage = String.format(ServiceUserConsentHistoryRepository.SERVICE_USER_CONSENTS_NOT_FOUND,
                serviceId, userId);
            throw new ResourceNotFoundException(errorMessage);
        }

        return consentHistories;
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

        List<ConsentChangeEvent> consentChangeEvents = consentHistoryStore.get(partitionKey);
        if (consentChangeEvents == null) {
            consentChangeEvents = new ArrayList<>();
        }
        consentChangeEvents.add(consentHistoryRecord);
        consentHistoryStore.put(partitionKey, consentChangeEvents);

        final String serviceUserId = getServiceUserId(serviceId, userId);
        List<ConsentHistory> consentHistories = consentHistoryByServiceUserStore.get(serviceUserId);
        if (consentHistories == null) {
            consentHistories = new ArrayList<>();
        }
        final ConsentHistory consentHistory = consentHistories.stream()
            .filter(consentHistoryItem -> consentHistoryItem.getConsentId().equals(partitionKey))
            .findFirst()
            .orElse(null);
        if (consentHistory == null) {
            final ConsentHistory consentHistoryItem = new ConsentHistory()
                .consentId(partitionKey)
                .addHistoryItem(consentHistoryRecord);
            consentHistories.add(consentHistoryItem);
        } else {
            consentHistory.addHistoryItem(consentHistoryRecord);
        }
        consentHistoryByServiceUserStore.put(serviceUserId, consentHistories);
    }

    /**
     * Generate a partition key for the given service ID, user ID, and consent ID.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     */
    public static String getPartitionKey(final String serviceId, final String userId, final String consentId) {
        return String.format("%s|%s|%s", serviceId, userId, consentId);
    }

    /**
     * Generate a service user ID for the given service ID and user ID.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     */
    public static String getServiceUserId(final String serviceId, final String userId) {
        return String.format("%s|%s", serviceId, userId);
    }
}
