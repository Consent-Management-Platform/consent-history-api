package com.consentframework.consenthistory.api.infrastructure.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class DynamoDbServiceUserConsentHistoryRepositoryTest {
    private final DynamoDbServiceUserConsentHistoryRepository repository = new DynamoDbServiceUserConsentHistoryRepository();

    @Test
    void testGetConsentHistoryWhenNotFound() throws Exception {
        final String expectedErrorMessage = String.format(ServiceUserConsentHistoryRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        final ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            repository.getConsentHistory(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                TestConstants.TEST_CONSENT_ID);
        });
        assertEquals(expectedErrorMessage, exception.getMessage());
    }
}
