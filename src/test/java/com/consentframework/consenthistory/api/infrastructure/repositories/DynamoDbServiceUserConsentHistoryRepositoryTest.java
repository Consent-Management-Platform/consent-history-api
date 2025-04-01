package com.consentframework.consenthistory.api.infrastructure.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.infrastructure.mappers.DynamoDbConsentChangeEventMapper;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
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
        final DynamoDbServiceUserConsentHistoryRecord record1 = DynamoDbServiceUserConsentHistoryRecordGenerator.generate();
        final DynamoDbServiceUserConsentHistoryRecord record2 = DynamoDbServiceUserConsentHistoryRecordGenerator.generate();
        final DynamoDbServiceUserConsentHistoryRecord record3 = DynamoDbServiceUserConsentHistoryRecordGenerator.generate();
        final Page<DynamoDbServiceUserConsentHistoryRecord> page1 = Page.builder(DynamoDbServiceUserConsentHistoryRecord.class)
            .items(List.of(record1, record2))
            .build();
        final Page<DynamoDbServiceUserConsentHistoryRecord> page2 = Page.builder(DynamoDbServiceUserConsentHistoryRecord.class)
            .items(List.of(record3))
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
