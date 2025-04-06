package com.consentframework.consenthistory.api.infrastructure.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.consentframework.consenthistory.api.domain.entities.StoredConsent;
import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.infrastructure.mappers.DynamoDbConsentChangeEventMapper;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.consenthistory.api.testcommon.utils.DynamoDbServiceUserConsentHistoryRecordGenerator;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

class DynamoDbServiceUserConsentHistoryRepositoryTest {
    @Mock
    private DynamoDbTable<DynamoDbServiceUserConsentHistoryRecord> consentHistoryTable;

    @Mock
    private PageIterable<DynamoDbServiceUserConsentHistoryRecord> queryResults;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        consentHistoryTable = (DynamoDbTable<DynamoDbServiceUserConsentHistoryRecord>) mock(DynamoDbTable.class);
        queryResults = mock(PageIterable.class);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetConsentHistoryWhenNullResults() throws Exception {
        when(consentHistoryTable.query(any(QueryEnhancedRequest.class))).thenReturn(null);
        final DynamoDbServiceUserConsentHistoryRepository repository = new DynamoDbServiceUserConsentHistoryRepository(consentHistoryTable);
        validateNotFoundResult(repository);
    }

    @Test
    void testGetConsentHistoryWhenEmptyResults() throws Exception {
        when(queryResults.stream()).thenReturn(Stream.empty());
        when(consentHistoryTable.query(any(QueryEnhancedRequest.class))).thenReturn(queryResults);
        final DynamoDbServiceUserConsentHistoryRepository repository = new DynamoDbServiceUserConsentHistoryRepository(consentHistoryTable);
        validateNotFoundResult(repository);
    }

    @Test
    void testGetConsentHistoryWhenHaveResults() throws Exception {
        final StoredConsent consentV1 = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final StoredConsent consentV2 = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");
        final StoredConsent consentV3 = DynamoDbServiceUserConsentHistoryRecordGenerator.generateDdbConsentImage("1");

        final DynamoDbServiceUserConsentHistoryRecord record1 = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            null, consentV1, ConsentEventType.INSERT, OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC)
        );
        final DynamoDbServiceUserConsentHistoryRecord record2 = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            consentV1, consentV2, ConsentEventType.MODIFY, OffsetDateTime.now().plusSeconds(1).withOffsetSameInstant(ZoneOffset.UTC)
        );
        final DynamoDbServiceUserConsentHistoryRecord record3 = DynamoDbServiceUserConsentHistoryRecordGenerator.generate(
            consentV2, consentV3, ConsentEventType.MODIFY, OffsetDateTime.now().plusSeconds(2).withOffsetSameInstant(ZoneOffset.UTC)
        );
        // Mock query results to return change events in different order than when they occurred to test sorting
        final Page<DynamoDbServiceUserConsentHistoryRecord> page1 = Page.builder(DynamoDbServiceUserConsentHistoryRecord.class)
            .items(List.of(record3, record2))
            .build();
        final Page<DynamoDbServiceUserConsentHistoryRecord> page2 = Page.builder(DynamoDbServiceUserConsentHistoryRecord.class)
            .items(List.of(record1))
            .build();
        when(queryResults.stream()).thenReturn(List.of(page1, page2).stream());
        when(consentHistoryTable.query(any(QueryEnhancedRequest.class))).thenReturn(queryResults);

        final DynamoDbServiceUserConsentHistoryRepository repository = new DynamoDbServiceUserConsentHistoryRepository(consentHistoryTable);
        final List<ConsentChangeEvent> retrievedConsentHistory = repository.getConsentHistory(TestConstants.TEST_SERVICE_ID,
            TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertEquals(3, retrievedConsentHistory.size());
        assertEquals(DynamoDbConsentChangeEventMapper.toConsentChangeEvent(record1), retrievedConsentHistory.get(0));
        assertEquals(DynamoDbConsentChangeEventMapper.toConsentChangeEvent(record2), retrievedConsentHistory.get(1));
        assertEquals(DynamoDbConsentChangeEventMapper.toConsentChangeEvent(record3), retrievedConsentHistory.get(2));
    }

    private void validateNotFoundResult(final DynamoDbServiceUserConsentHistoryRepository repository) {
        final String expectedErrorMessage = String.format(ServiceUserConsentHistoryRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        final ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            repository.getConsentHistory(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                TestConstants.TEST_CONSENT_ID);
        });
        assertEquals(expectedErrorMessage, exception.getMessage());
    }
}
