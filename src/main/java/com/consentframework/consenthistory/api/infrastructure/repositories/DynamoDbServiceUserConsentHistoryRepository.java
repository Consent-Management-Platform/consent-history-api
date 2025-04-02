package com.consentframework.consenthistory.api.infrastructure.repositories;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.infrastructure.mappers.DynamoDbConsentChangeEventMapper;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

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
    public List<ConsentChangeEvent> getConsentHistory(String serviceId, String userId, String consentId)
            throws ResourceNotFoundException {
        final QueryEnhancedRequest queryRequest = buildGetConsentHistoryQueryRequest(serviceId, userId, consentId);
        final PageIterable<DynamoDbServiceUserConsentHistoryRecord> queryResults = consentHistoryTable.query(queryRequest);
        if (queryResults == null) {
            throwNotFoundError(serviceId, userId, consentId);
        }

        final List<ConsentChangeEvent> consentHistoryRecords = queryResults.stream()
            .flatMap(page -> page.items().stream())
            .map(ddbHistoryRecord -> DynamoDbConsentChangeEventMapper.toConsentChangeEvent(ddbHistoryRecord))
            .toList();

        if (consentHistoryRecords.isEmpty()) {
            throwNotFoundError(serviceId, userId, consentId);
        }
        return consentHistoryRecords;
    }

    private QueryEnhancedRequest buildGetConsentHistoryQueryRequest(final String serviceId, final String userId, final String consentId) {
        final String partitionKey = String.format("%s|%s|%s", serviceId, userId, consentId);

        final Key queryKey = Key.builder()
            .partitionValue(partitionKey)
            .sortValue(serviceId)
            .build();

        return QueryEnhancedRequest.builder()
            .queryConditional(QueryConditional.keyEqualTo(queryKey))
            .build();
    }

    private void throwNotFoundError(String serviceId, String userId, String consentId)
            throws ResourceNotFoundException {
        final String notFoundMessage = String.format(CONSENT_NOT_FOUND_MESSAGE,
            serviceId, userId, consentId);
        logger.warn(notFoundMessage);
        throw new ResourceNotFoundException(notFoundMessage);
    }
}
