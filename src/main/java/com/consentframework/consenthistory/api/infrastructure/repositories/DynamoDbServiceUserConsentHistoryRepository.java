package com.consentframework.consenthistory.api.infrastructure.repositories;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.infrastructure.mappers.DynamoDbConsentChangeEventMapper;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentHistory;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DynamoDB implementation of the ServiceUserConsentHistory repository.
 */
public class DynamoDbServiceUserConsentHistoryRepository implements ServiceUserConsentHistoryRepository {
    private static final Logger logger = LogManager.getLogger(DynamoDbServiceUserConsentHistoryRepository.class);

    private final DynamoDbTable<DynamoDbServiceUserConsentHistoryRecord> consentHistoryTable;

    /**
     * Construct the DynamoDB consent history repository.
     *
     * @param consentHistoryTable DynamoDB table storing consent history records.
     */
    public DynamoDbServiceUserConsentHistoryRepository(final DynamoDbTable<DynamoDbServiceUserConsentHistoryRecord> consentHistoryTable) {
        this.consentHistoryTable = consentHistoryTable;
    }

    /**
     * Retrieve history for a given service user consent.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     * @throws ResourceNotFoundException if no history is found for the given service-user-consent ID
     */
    @Override
    public List<ConsentChangeEvent> getConsentHistory(final String serviceId, final String userId, final String consentId)
            throws ResourceNotFoundException {
        final QueryEnhancedRequest queryRequest = buildGetConsentHistoryQueryRequest(serviceId, userId, consentId);
        final PageIterable<DynamoDbServiceUserConsentHistoryRecord> queryResults = consentHistoryTable.query(queryRequest);
        if (queryResults == null) {
            throwNotFoundError(serviceId, userId, consentId);
        }

        final List<ConsentChangeEvent> consentHistoryRecords = queryResults.stream()
            .flatMap(page -> page.items().stream())
            .map(DynamoDbConsentChangeEventMapper::toConsentChangeEvent)
            .toList();

        if (consentHistoryRecords.isEmpty()) {
            throwNotFoundError(serviceId, userId, consentId);
        }
        return consentHistoryRecords;
    }

    /**
     * Retrieve history for a given service user.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @throws ResourceNotFoundException if no history is found for the given service user
     */
    @Override
    public List<ConsentHistory> getServiceUserHistory(final String serviceId, final String userId) throws ResourceNotFoundException {
        final QueryEnhancedRequest queryRequest = buildGetConsentHistoryByServiceUserQueryRequest(serviceId, userId);
        final SdkIterable<Page<DynamoDbServiceUserConsentHistoryRecord>> queryResults = consentHistoryTable
            .index(DynamoDbServiceUserConsentHistoryRecord.CONSENT_HISTORY_BY_SERVICE_USER_GSI_NAME)
            .query(queryRequest);

        final Map<String, List<ConsentChangeEvent>> consentHistoryByConsentId = parseConsentIdsToChangeEvents(
            serviceId, userId, queryResults);

        final List<ConsentHistory> consentHistories = consentHistoryByConsentId.entrySet()
            .stream()
            .map(this::parseConsentHistory)
            .collect(Collectors.toList());

        logger.info("getConsentHistory({}, {}) retrieved {} consent histories",
            serviceId, userId, consentHistories.size());
        if (consentHistories.isEmpty()) {
            throwNotFoundError(serviceId, userId);
        }
        return consentHistories;
    }

    /**
     * Parse consent history query results into a map of consent IDs to consent change events.
     *
     * @param queryResults ConsentHistory DynamoDB query results
     * @return Map of consent ID to list of consent change events
     * @throws ResourceNotFoundException if no history was found for the given service user
     */
    private Map<String, List<ConsentChangeEvent>> parseConsentIdsToChangeEvents(final String serviceId, final String userId,
            final SdkIterable<Page<DynamoDbServiceUserConsentHistoryRecord>> queryResults) throws ResourceNotFoundException {
        if (queryResults == null) {
            throwNotFoundError(serviceId, userId);
        }

        final Map<String, List<ConsentChangeEvent>> consentHistoryByConsentId = new HashMap<>();

        queryResults.stream().forEach(pageResults -> {
            pageResults.items().forEach(ddbHistoryRecord -> {
                final ConsentChangeEvent consentChangeEvent = DynamoDbConsentChangeEventMapper.toConsentChangeEvent(ddbHistoryRecord);
                final String consentId = consentChangeEvent.getConsentId();
                logger.info("getConsentHistory({}, {}) retrieved consent change event with consentId: {}, eventId: {}",
                    serviceId, userId, consentId, consentChangeEvent.getEventId());
                if (consentHistoryByConsentId.containsKey(consentId)) {
                    consentHistoryByConsentId.get(consentId).add(consentChangeEvent);
                } else {
                    final List<ConsentChangeEvent> consentHistory = new ArrayList<>();
                    consentHistory.add(consentChangeEvent);
                    consentHistoryByConsentId.put(consentId, consentHistory);
                }
            });
        });

        return consentHistoryByConsentId;
    }

    private ConsentHistory parseConsentHistory(final Map.Entry<String, List<ConsentChangeEvent>> consentIdAndChangeEvents) {
        return new ConsentHistory()
            .consentId(consentIdAndChangeEvents.getKey())
            .history(consentIdAndChangeEvents.getValue());
    }

    private QueryEnhancedRequest buildGetConsentHistoryQueryRequest(final String serviceId, final String userId, final String consentId) {
        final String partitionKey = String.format("%s|%s|%s", serviceId, userId, consentId);

        final Key queryKey = Key.builder()
            .partitionValue(partitionKey)
            .build();
        logger.info("Built DDB query Key with partitionValue {}", partitionKey);

        return QueryEnhancedRequest.builder()
            .queryConditional(QueryConditional.keyEqualTo(queryKey))
            .scanIndexForward(true) // Sort ascending by event time (oldest events first)
            .build();
    }

    private QueryEnhancedRequest buildGetConsentHistoryByServiceUserQueryRequest(final String serviceId, final String userId) {
        final String serviceUserId = String.format("%s|%s", serviceId, userId);

        final Key queryKey = Key.builder()
            .partitionValue(serviceUserId)
            .build();
        logger.info("Built ConsentHistoryByServiceUser GSI query Key with partitionValue {}", serviceUserId);

        return QueryEnhancedRequest.builder()
            .queryConditional(QueryConditional.keyEqualTo(queryKey))
            .scanIndexForward(true) // Sort ascending by event time (oldest events first)
            .build();
    }

    private void throwNotFoundError(final String serviceId, final String userId, final String consentId)
            throws ResourceNotFoundException {
        final String notFoundMessage = String.format(CONSENT_NOT_FOUND_MESSAGE,
            serviceId, userId, consentId);
        logger.warn(notFoundMessage);
        throw new ResourceNotFoundException(notFoundMessage);
    }

    private void throwNotFoundError(final String serviceId, final String userId) throws ResourceNotFoundException {
        final String notFoundMessage = String.format(SERVICE_USER_CONSENTS_NOT_FOUND,
            serviceId, userId);
        logger.warn(notFoundMessage);
        throw new ResourceNotFoundException(notFoundMessage);
    }
}
