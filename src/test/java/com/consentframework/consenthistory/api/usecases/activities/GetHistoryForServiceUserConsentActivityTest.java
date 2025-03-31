package com.consentframework.consenthistory.api.usecases.activities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.consentframework.consenthistory.api.infrastructure.repositories.InMemoryServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.GetHistoryForServiceUserConsentResponseContent;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.consenthistory.api.testcommon.utils.ConsentChangeEventGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class GetHistoryForServiceUserConsentActivityTest {
    private InMemoryServiceUserConsentHistoryRepository consentHistoryRepository;
    private GetHistoryForServiceUserConsentActivity activity;

    @BeforeEach
    void setUp() {
        consentHistoryRepository = new InMemoryServiceUserConsentHistoryRepository();
        activity = new GetHistoryForServiceUserConsentActivity(consentHistoryRepository);
    }

    @Test
    void testRetrieveNonExistingConsent() throws Exception {
        final GetHistoryForServiceUserConsentResponseContent response = activity.handleRequest(
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertNull(response.getData());
    }

    @Test
    void testRetrieveHistoryForExistingConsent() throws Exception {
        final ConsentChangeEvent consentChangeEvent1 = ConsentChangeEventGenerator.generate();
        consentHistoryRepository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
            TestConstants.TEST_CONSENT_ID, consentChangeEvent1);

        final ConsentChangeEvent consentChangeEvent2 = ConsentChangeEventGenerator.generate();
        consentHistoryRepository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
            TestConstants.TEST_CONSENT_ID, consentChangeEvent2);

        final GetHistoryForServiceUserConsentResponseContent response = activity.handleRequest(
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        final List<ConsentChangeEvent> consentHistory = response.getData();
        assertEquals(2, consentHistory.size());
        assertEquals(consentChangeEvent1, consentHistory.get(0));
        assertEquals(consentChangeEvent2, consentHistory.get(1));
    }
}
