package com.consentframework.consenthistory.api.usecases.activities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.infrastructure.repositories.InMemoryServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.models.ConsentHistory;
import com.consentframework.consenthistory.api.models.GetHistoryForServiceUserResponseContent;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.consenthistory.api.testcommon.utils.ConsentChangeEventGenerator;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

class GetHistoryForServiceUserActivityTest {
    private InMemoryServiceUserConsentHistoryRepository consentHistoryRepository;
    private GetHistoryForServiceUserActivity activity;

    @BeforeEach
    void setUp() {
        consentHistoryRepository = new InMemoryServiceUserConsentHistoryRepository();
        activity = new GetHistoryForServiceUserActivity(consentHistoryRepository);
    }

    @Test
    void testHandleRequestWhenNoConsent() throws Exception {
        final String expectedErrorMessage = String.format(ServiceUserConsentHistoryRepository.SERVICE_USER_CONSENTS_NOT_FOUND,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID);
        final ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            activity.handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID));
        assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void testHandleRequestWhenHaveConsents() throws Exception {
        final ConsentChangeEvent consent1ChangeEvent1 = ConsentChangeEventGenerator.generate();
        consentHistoryRepository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
            TestConstants.TEST_CONSENT_ID, consent1ChangeEvent1);

        final ConsentChangeEvent consent2ChangeEvent1 = new ConsentChangeEvent()
            .consentId(TestConstants.TEST_PARTITION_KEY_2)
            .eventId(UUID.randomUUID().toString())
            .eventTime(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC))
            .eventType(ConsentEventType.INSERT);
        consentHistoryRepository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
            TestConstants.TEST_CONSENT_ID_2, consent2ChangeEvent1);

        final ConsentChangeEvent consent1ChangeEvent2 = ConsentChangeEventGenerator.generate();
        consentHistoryRepository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
            TestConstants.TEST_CONSENT_ID, consent1ChangeEvent2);

        final GetHistoryForServiceUserResponseContent response = activity.handleRequest(
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID);
        final List<ConsentHistory> retrievedConsentHistories = response.getData();
        assertEquals(2, retrievedConsentHistories.size());

        final ConsentHistory retrievedConsent1History = retrievedConsentHistories.get(0);
        assertEquals(TestConstants.TEST_PARTITION_KEY, retrievedConsent1History.getConsentId());
        assertEquals(List.of(consent1ChangeEvent1, consent1ChangeEvent2), retrievedConsent1History.getHistory());

        final ConsentHistory retrievedConsent2History = retrievedConsentHistories.get(1);
        assertEquals(TestConstants.TEST_PARTITION_KEY_2, retrievedConsent2History.getConsentId());
        assertEquals(List.of(consent2ChangeEvent1), retrievedConsent2History.getHistory());
    }
}
