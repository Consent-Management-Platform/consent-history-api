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
import com.consentframework.consenthistory.api.models.GetHistoryForServiceUserConsentResponseContent;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import com.consentframework.consenthistory.api.testcommon.utils.ConsentChangeEventGenerator;
import com.consentframework.consenthistory.api.usecases.activities.GetHistoryForServiceUserConsentActivity;
import com.consentframework.shared.api.domain.constants.HttpMethod;
import com.consentframework.shared.api.domain.constants.HttpStatusCode;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class GetHistoryForServiceUserConsentRequestHandlerTest extends RequestHandlerTest {
    private final InMemoryServiceUserConsentHistoryRepository repository = new InMemoryServiceUserConsentHistoryRepository();
    private final GetHistoryForServiceUserConsentActivity activity = new GetHistoryForServiceUserConsentActivity(repository);
    private final GetHistoryForServiceUserConsentRequestHandler handler = new GetHistoryForServiceUserConsentRequestHandler(activity);

    private static final Map<String, String> VALID_PATH_PARAMS = Map.of(
        ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
        ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID,
        ApiPathParameterName.CONSENT_ID.getValue(), TestConstants.TEST_CONSENT_ID
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

        final String expectedErrorMessage = String.format(ServiceUserConsentHistoryRepository.CONSENT_NOT_FOUND_MESSAGE,
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertExceptionResponse(HttpStatusCode.NOT_FOUND, expectedErrorMessage, response);
    }

    @Test
    void testHandleRequestWhenConsentFound() throws Exception {
        final ConsentChangeEvent consentChangeEvent1 = ConsentChangeEventGenerator.generate();
        final ConsentChangeEvent consentChangeEvent2 = ConsentChangeEventGenerator.generate();
        repository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID,
            consentChangeEvent1);
        repository.addConsentHistoryRecord(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID,
            consentChangeEvent2);

        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMS, null);
        final Map<String, Object> response = handler.handleRequest(request);
        assertSuccessResponse(response);

        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof String);

        final GetHistoryForServiceUserConsentResponseContent responseContent = new JSON().getMapper()
            .readValue((String) responseBody, GetHistoryForServiceUserConsentResponseContent.class);
        final List<ConsentChangeEvent> retrievedConsentHistory = responseContent.getData();
        assertEquals(2, retrievedConsentHistory.size());
        assertEquals(consentChangeEvent1, retrievedConsentHistory.get(0));
        assertEquals(consentChangeEvent2, retrievedConsentHistory.get(1));
    }

    @Test
    void testHandleRequestWhenErrorThrown() throws Exception {
        final String testErrorMessage = "Test exception";
        final GetHistoryForServiceUserConsentActivity mockActivity = mock(GetHistoryForServiceUserConsentActivity.class);
        doThrow(new ResourceNotFoundException(testErrorMessage))
            .when(mockActivity).handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        final GetHistoryForServiceUserConsentRequestHandler mockHandler = new GetHistoryForServiceUserConsentRequestHandler(mockActivity);

        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMS, null);
        final Map<String, Object> response = mockHandler.handleRequest(request);
        assertExceptionResponse(HttpStatusCode.NOT_FOUND, testErrorMessage, response);
    }

    private ApiRequest buildApiRequest(final Map<String, String> pathParameters, final Map<String, Object> queryStringParameters) {
        return new ApiRequest(HttpMethod.GET.name(), ApiHttpResource.SERVICE_USER_CONSENT_HISTORY.getValue(),
            TestConstants.TEST_CONSENT_HISTORY_PATH, pathParameters, queryStringParameters, null, false, null);
    }

    private void assertMissingPathParametersResponse(final Map<String, Object> response) {
        assertExceptionResponse(
            HttpStatusCode.BAD_REQUEST,
            "Missing required path parameters, expected serviceId, userId, consentId",
            response
        );
    }
}
