package com.consentframework.consenthistory.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.consentframework.consenthistory.api.domain.constants.ApiHttpResource;
import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.infrastructure.repositories.InMemoryServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.consenthistory.api.testcommon.utils.ConsentChangeEventGenerator;
import com.consentframework.shared.api.domain.constants.ApiResponseParameterName;
import com.consentframework.shared.api.domain.constants.HttpMethod;
import com.consentframework.shared.api.domain.constants.HttpStatusCode;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

class ConsentHistoryApiServiceTest {
    @Test
    void handleNullRequest() {
        final ServiceUserConsentHistoryRepository repository = new InMemoryServiceUserConsentHistoryRepository();
        final ConsentHistoryApiService service = new ConsentHistoryApiService(repository);

        final Map<String, Object> response = service.handleRequest(null, null);
        assertNotNull(response);
        assertEquals(HttpStatusCode.BAD_REQUEST.getValue(), response.get(ApiResponseParameterName.STATUS_CODE.getValue()));
        assertEquals(
            "{\"message\":\"Unsupported resource operation, received resource 'null' and operation 'null'\"}",
            response.get(ApiResponseParameterName.BODY.getValue())
        );
    }

    @Test
    void handleRequestForUnsupportedResource() {
        final String unsupportedResource = "/v1/consent-history/services";
        validateUnsupportedOperation(HttpMethod.GET, unsupportedResource, unsupportedResource, null);
    }

    @Test
    void handleRequestForUnsupportedResourceMethod() {
        validateUnsupportedOperation(
            HttpMethod.POST,
            ApiHttpResource.SERVICE_USER_CONSENT_HISTORY.getValue(),
            "/v1/consent-history/services/testServiceId/users/testUserId/consents/testConsentId",
            "{\"someKey\":\"someBody\"}"
        );
    }

    @Test
    void handleRequestGetHistoryWhenNotFound() {
        final ApiRequest request = new ApiRequest(
            HttpMethod.GET.name(),
            ApiHttpResource.SERVICE_USER_CONSENT_HISTORY.getValue(),
            TestConstants.TEST_CONSENT_HISTORY_PATH,
            TestConstants.TEST_CONSENT_HISTORY_PATH_PARAMS,
            null,
            null,
            false,
            null
        );
        final ServiceUserConsentHistoryRepository repository = new InMemoryServiceUserConsentHistoryRepository();
        final ConsentHistoryApiService service = new ConsentHistoryApiService(repository);

        final Map<String, Object> response = service.handleRequest(request, null);
        assertNotNull(response);
        assertEquals(HttpStatusCode.NOT_FOUND.getValue(), response.get(ApiResponseParameterName.STATUS_CODE.getValue()));
        assertEquals(
            String.format(
                "{\"message\":\"%s\"}",
                String.format(ServiceUserConsentHistoryRepository.CONSENT_NOT_FOUND_MESSAGE,
                    TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID)
            ),
            response.get(ApiResponseParameterName.BODY.getValue())
        );
    }

    @Test
    void handleRequestGetHistoryWhenFound() {
        final ApiRequest request = new ApiRequest(
            HttpMethod.GET.name(),
            ApiHttpResource.SERVICE_USER_CONSENT_HISTORY.getValue(),
            TestConstants.TEST_CONSENT_HISTORY_PATH,
            TestConstants.TEST_CONSENT_HISTORY_PATH_PARAMS,
            null,
            null,
            false,
            null
        );
        final InMemoryServiceUserConsentHistoryRepository repository = new InMemoryServiceUserConsentHistoryRepository();
        final ConsentChangeEvent consentChangeEvent = ConsentChangeEventGenerator.generate();
        repository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID,
            consentChangeEvent);
        final ConsentHistoryApiService service = new ConsentHistoryApiService(repository);

        final Map<String, Object> response = service.handleRequest(request, null);
        assertNotNull(response);
        System.out.println(response.toString());
        assertEquals(HttpStatusCode.SUCCESS.getValue(), response.get(ApiResponseParameterName.STATUS_CODE.getValue()));
        assertEquals(
            String.format(
                "{\"data\":[{\"consentId\":\"%s\",\"eventId\":\"%s\",\"eventTime\":\"%s\",\"eventType\":\"%s\"}]}",
                consentChangeEvent.getConsentId(),
                consentChangeEvent.getEventId(),
                consentChangeEvent.getEventTime().atZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                consentChangeEvent.getEventType().name()
            ),
            response.get(ApiResponseParameterName.BODY.getValue())
        );
    }

    private void validateUnsupportedOperation(final HttpMethod httpMethod, final String resource, final String path,
            final String requestBody) {
        final ApiRequest request = new ApiRequest(
            httpMethod.name(),
            resource,
            path,
            TestConstants.TEST_CONSENT_HISTORY_PATH_PARAMS,
            null,
            null,
            false,
            requestBody
        );
        final ServiceUserConsentHistoryRepository repository = new InMemoryServiceUserConsentHistoryRepository();
        final ConsentHistoryApiService service = new ConsentHistoryApiService(repository);

        final Map<String, Object> response = service.handleRequest(request, null);
        assertNotNull(response);
        assertEquals(HttpStatusCode.BAD_REQUEST.getValue(), response.get(ApiResponseParameterName.STATUS_CODE.getValue()));
        assertEquals(
            String.format(
                "{\"message\":\"Unsupported resource operation, received resource '%s' and operation '%s'\"}",
                resource, httpMethod.name()
            ),
            response.get(ApiResponseParameterName.BODY.getValue())
        );
    }
}
