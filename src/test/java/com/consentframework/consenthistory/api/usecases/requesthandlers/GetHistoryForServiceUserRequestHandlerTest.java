package com.consentframework.consenthistory.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.consentframework.consenthistory.api.JSON;
import com.consentframework.consenthistory.api.domain.constants.ApiHttpResource;
import com.consentframework.consenthistory.api.domain.constants.ApiPathParameterName;
import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.infrastructure.repositories.InMemoryServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.ConsentEventType;
import com.consentframework.consenthistory.api.models.ConsentHistory;
import com.consentframework.consenthistory.api.models.GetHistoryForServiceUserResponseContent;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.consenthistory.api.testcommon.utils.ConsentChangeEventGenerator;
import com.consentframework.consenthistory.api.usecases.activities.GetHistoryForServiceUserActivity;
import com.consentframework.shared.api.domain.constants.HttpMethod;
import com.consentframework.shared.api.domain.constants.HttpStatusCode;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.exceptions.InternalServiceException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class GetHistoryForServiceUserRequestHandlerTest extends RequestHandlerTest {
    private final InMemoryServiceUserConsentHistoryRepository repository = new InMemoryServiceUserConsentHistoryRepository();
    private final GetHistoryForServiceUserActivity activity = new GetHistoryForServiceUserActivity(repository);
    private final GetHistoryForServiceUserRequestHandler handler = new GetHistoryForServiceUserRequestHandler(activity);

    private static final Map<String, String> VALID_PATH_PARAMS = Map.of(
        ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
        ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID
    );

    @Test
    protected void testHandleNullRequest() throws Exception {
        final Map<String, Object> response = handler.handleRequest(null);
        assertMissingPathParametersResponse(response);
    }

    @Test
    protected void testHandleRequestMissingPathParameters() throws Exception {
        final Map<String, String> incompletePathParameters = Map.of(
            ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID);
        final ApiRequest request = buildApiRequest(incompletePathParameters, null);

        final Map<String, Object> response = handler.handleRequest(request);
        assertMissingPathParametersResponse(response);
    }

    @Test
    void testHandleRequestWhenConsentNotFound() {
        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMS, null);
        final Map<String, Object> response = handler.handleRequest(request);

        final String expectedErrorMessage = String.format(ServiceUserConsentHistoryRepository.SERVICE_USER_CONSENTS_NOT_FOUND,
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID);
        assertExceptionResponse(HttpStatusCode.NOT_FOUND, expectedErrorMessage, response);
    }

    @Test
    void testHandleRequestWhenErrorThrown() throws Exception {
        final String testErrorMessage = "Test exception";
        final GetHistoryForServiceUserActivity mockActivity = mock(GetHistoryForServiceUserActivity.class);
        doThrow(new InternalServiceException(testErrorMessage))
            .when(mockActivity).handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID);
        final GetHistoryForServiceUserRequestHandler mockHandler = new GetHistoryForServiceUserRequestHandler(mockActivity);

        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMS, null);
        final Map<String, Object> response = mockHandler.handleRequest(request);
        assertExceptionResponse(HttpStatusCode.INTERNAL_SERVER_ERROR, testErrorMessage, response);
    }

    @Test
    void testHandleRequestWhenConsentsFound() throws Exception {
        final ConsentChangeEvent consent1ChangeEvent1 = ConsentChangeEventGenerator.generate();
        final ConsentChangeEvent consent1ChangeEvent2 = ConsentChangeEventGenerator.generate();
        final ConsentChangeEvent consent2ChangeEvent1 = new ConsentChangeEvent()
            .consentId(TestConstants.TEST_PARTITION_KEY_2)
            .eventId(UUID.randomUUID().toString())
            .eventTime(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC))
            .eventType(ConsentEventType.INSERT);
        repository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID,
            consent1ChangeEvent1);
        repository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
            TestConstants.TEST_CONSENT_ID_2, consent2ChangeEvent1);
        repository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID,
            consent1ChangeEvent2);

        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMS, null);
        final Map<String, Object> response = handler.handleRequest(request);
        assertSuccessResponse(response);

        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof String);

        final GetHistoryForServiceUserResponseContent responseContent = new JSON().getMapper()
            .readValue((String) responseBody, GetHistoryForServiceUserResponseContent.class);
        final List<ConsentHistory> retrievedConsentHistories = responseContent.getData();
        assertEquals(2, retrievedConsentHistories.size());
        final ConsentHistory retrievedConsentHistory1 = retrievedConsentHistories.get(0);
        assertEquals(TestConstants.TEST_PARTITION_KEY, retrievedConsentHistory1.getConsentId());
        assertEquals(List.of(consent1ChangeEvent1, consent1ChangeEvent2), retrievedConsentHistory1.getHistory());
        final ConsentHistory retrievedConsentHistory2 = retrievedConsentHistories.get(1);
        assertEquals(TestConstants.TEST_PARTITION_KEY_2, retrievedConsentHistory2.getConsentId());
        assertEquals(List.of(consent2ChangeEvent1), retrievedConsentHistory2.getHistory());
    }

    private ApiRequest buildApiRequest(final Map<String, String> pathParameters, final Map<String, Object> queryStringParameters) {
        return new ApiRequest(HttpMethod.GET.name(), ApiHttpResource.SERVICE_USER_HISTORY.getValue(),
            TestConstants.TEST_CONSENTS_HISTORY_PATH, pathParameters, queryStringParameters, null, false, null);
    }

    private void assertMissingPathParametersResponse(final Map<String, Object> response) {
        assertExceptionResponse(
            HttpStatusCode.BAD_REQUEST,
            "Missing required path parameters, expected serviceId, userId",
            response
        );
    }
}
