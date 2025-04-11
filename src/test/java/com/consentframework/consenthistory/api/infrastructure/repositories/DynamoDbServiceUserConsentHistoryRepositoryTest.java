package com.consentframework.consenthistory.api.infrastructure.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.consentframework.consenthistory.api.models.ConsentHistory;
import com.consentframework.consenthistory.api.models.ConsentStatus;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.consenthistory.api.testcommon.utils.DynamoDbServiceUserConsentHistoryRecordGenerator;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

class DynamoDbServiceUserConsentHistoryRepositoryTest {
    @Mock
    private DynamoDbTable<DynamoDbServiceUserConsentHistoryRecord> consentHistoryTable;

    @Mock
    private PageIterable<DynamoDbServiceUserConsentHistoryRecord> queryResults;

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
    final Page<DynamoDbServiceUserConsentHistoryRecord> page1 = Page.builder(DynamoDbServiceUserConsentHistoryRecord.class)
        .items(List.of(record1, record2))
        .build();
    final Page<DynamoDbServiceUserConsentHistoryRecord> page2 = Page.builder(DynamoDbServiceUserConsentHistoryRecord.class)
        .items(List.of(record3))
        .build();

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
        validateConsentHistoryNotFoundResult(repository);
    }

    @Test
    void testGetConsentHistoryWhenEmptyResults() throws Exception {
        when(queryResults.stream()).thenReturn(Stream.empty());
        when(consentHistoryTable.query(any(QueryEnhancedRequest.class))).thenReturn(queryResults);
        final DynamoDbServiceUserConsentHistoryRepository repository = new DynamoDbServiceUserConsentHistoryRepository(consentHistoryTable);
        validateConsentHistoryNotFoundResult(repository);
    }

    @Test
    void testGetConsentHistoryWhenHaveResults() throws Exception {
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

    @Test
    void testGetServiceUserHistoryWhenNullResults() throws Exception {
        @SuppressWarnings("unchecked")
        final DynamoDbIndex<DynamoDbServiceUserConsentHistoryRecord> index = mock(DynamoDbIndex.class);
        when(consentHistoryTable.index(DynamoDbServiceUserConsentHistoryRecord.CONSENT_HISTORY_BY_SERVICE_USER_GSI_NAME)).thenReturn(index);
        when(index.query(any(QueryEnhancedRequest.class))).thenReturn(null);
        final DynamoDbServiceUserConsentHistoryRepository repository = new DynamoDbServiceUserConsentHistoryRepository(consentHistoryTable);
        validateServiceUserHistoryNotFoundResult(repository);
    }

    @Test
    void testGetServiceUserHistoryWhenEmptyResults() throws Exception {
        @SuppressWarnings("unchecked")
        final DynamoDbIndex<DynamoDbServiceUserConsentHistoryRecord> index = mock(DynamoDbIndex.class);
        when(consentHistoryTable.index(DynamoDbServiceUserConsentHistoryRecord.CONSENT_HISTORY_BY_SERVICE_USER_GSI_NAME)).thenReturn(index);
        when(index.query(any(QueryEnhancedRequest.class))).thenReturn(queryResults);
        when(queryResults.stream()).thenReturn(Stream.empty());
        final DynamoDbServiceUserConsentHistoryRepository repository = new DynamoDbServiceUserConsentHistoryRepository(consentHistoryTable);
        validateServiceUserHistoryNotFoundResult(repository);
    }

    @Test
    void testGetServiceUserHistoryWhenHaveResults() throws Exception {
        final StoredConsent consentWithDistinctId = new StoredConsent()
            .serviceId(TestConstants.TEST_SERVICE_ID)
            .userId(TestConstants.TEST_USER_ID)
            .consentId(TestConstants.TEST_CONSENT_ID_2)
            .consentVersion(1)
            .consentStatus(ConsentStatus.ACTIVE)
            .consentType(TestConstants.TEST_CONSENT_TYPE);
        final DynamoDbServiceUserConsentHistoryRecord historyRecordForDistinctConsent = DynamoDbServiceUserConsentHistoryRecord.builder()
            .id(TestConstants.TEST_PARTITION_KEY_2)
            .eventId(UUID.randomUUID().toString())
            .eventType(ConsentEventType.INSERT.name())
            .eventTime(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toString())
            .serviceUserId(TestConstants.TEST_SERVICE_USER_ID)
            .oldImage(null)
            .newImage(consentWithDistinctId)
            .build();
        final Page<DynamoDbServiceUserConsentHistoryRecord> pageForDistinctConsent =
            Page.builder(DynamoDbServiceUserConsentHistoryRecord.class)
                .items(List.of(historyRecordForDistinctConsent))
                .build();

        when(queryResults.stream()).thenReturn(List.of(page1, pageForDistinctConsent, page2).stream());

        @SuppressWarnings("unchecked")
        final DynamoDbIndex<DynamoDbServiceUserConsentHistoryRecord> index = mock(DynamoDbIndex.class);
        when(consentHistoryTable.index(DynamoDbServiceUserConsentHistoryRecord.CONSENT_HISTORY_BY_SERVICE_USER_GSI_NAME)).thenReturn(index);
        when(index.query(any(QueryEnhancedRequest.class))).thenReturn(queryResults);

        final DynamoDbServiceUserConsentHistoryRepository repository = new DynamoDbServiceUserConsentHistoryRepository(consentHistoryTable);
        final List<ConsentHistory> retrievedConsentHistories = repository.getServiceUserHistory(
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID);
        assertNotNull(retrievedConsentHistories);
        assertEquals(2, retrievedConsentHistories.size());

        final ConsentHistory firstConsentHistory = retrievedConsentHistories.get(0);
        assertEquals(TestConstants.TEST_PARTITION_KEY, firstConsentHistory.getConsentId());
        final List<ConsentChangeEvent> firstConsentEvents = firstConsentHistory.getHistory();
        assertEquals(3, firstConsentEvents.size());
        assertEquals(DynamoDbConsentChangeEventMapper.toConsentChangeEvent(record1), firstConsentEvents.get(0));
        assertEquals(DynamoDbConsentChangeEventMapper.toConsentChangeEvent(record2), firstConsentEvents.get(1));
        assertEquals(DynamoDbConsentChangeEventMapper.toConsentChangeEvent(record3), firstConsentEvents.get(2));

        final ConsentHistory secondConsentHistory = retrievedConsentHistories.get(1);
        assertEquals(TestConstants.TEST_PARTITION_KEY_2, secondConsentHistory.getConsentId());
        final List<ConsentChangeEvent> secondConsentEvents = secondConsentHistory.getHistory();
        assertEquals(1, secondConsentEvents.size());
        assertEquals(DynamoDbConsentChangeEventMapper.toConsentChangeEvent(historyRecordForDistinctConsent), secondConsentEvents.get(0));
    }

    private void validateConsentHistoryNotFoundResult(final DynamoDbServiceUserConsentHistoryRepository repository) {
        final String expectedErrorMessage = String.format(ServiceUserConsentHistoryRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        final ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            repository.getConsentHistory(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                TestConstants.TEST_CONSENT_ID);
        });
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    private void validateServiceUserHistoryNotFoundResult(final DynamoDbServiceUserConsentHistoryRepository repository) throws Exception {
        final String expectedErrorMessage = String.format(ServiceUserConsentHistoryRepository.SERVICE_USER_CONSENTS_NOT_FOUND,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID);
        final ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            repository.getServiceUserHistory(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID);
        });
        assertEquals(expectedErrorMessage, exception.getMessage());
    }
}
