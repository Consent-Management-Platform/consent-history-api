package com.consentframework.consenthistory.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.consentframework.consenthistory.api.domain.constants.ApiHttpResource;
import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.infrastructure.entities.DynamoDbServiceUserConsentHistoryRecord;
import com.consentframework.consenthistory.api.infrastructure.repositories.DynamoDbServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.usecases.activities.GetHistoryForServiceUserConsentActivity;
import com.consentframework.consenthistory.api.usecases.requesthandlers.GetHistoryForServiceUserConsentRequestHandler;
import com.consentframework.shared.api.domain.constants.ApiResponseParameterName;
import com.consentframework.shared.api.domain.constants.HttpMethod;
import com.consentframework.shared.api.domain.constants.HttpStatusCode;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.requesthandlers.ApiRequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * Entry point for the service, handles requests for an AWS Lambda function.
 */
public class ConsentHistoryApiService implements RequestHandler<ApiRequest, Map<String, Object>> {
    static final String UNSUPPORTED_OPERATION_MESSAGE = "Unsupported resource operation, received resource '%s' and operation '%s'";
    private static final Logger logger = LogManager.getLogger(ConsentHistoryApiService.class);

    private ServiceUserConsentHistoryRepository consentHistoryRepository;

    /**
     * Instantiate API service.
     */
    public ConsentHistoryApiService() {
        final DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.create();
        final DynamoDbTable<DynamoDbServiceUserConsentHistoryRecord> dynamoDbTable = dynamoDbEnhancedClient.table(
            DynamoDbServiceUserConsentHistoryRecord.TABLE_NAME,
            TableSchema.fromImmutableClass(DynamoDbServiceUserConsentHistoryRecord.class));
        this.consentHistoryRepository = new DynamoDbServiceUserConsentHistoryRepository(dynamoDbTable);
    }

    /**
     * Instantiate API service with input repository.
     */
    public ConsentHistoryApiService(final ServiceUserConsentHistoryRepository consentHistoryRepository) {
        this.consentHistoryRepository = consentHistoryRepository;
    }

    /**
     * Route requests to appropriate request handler and return their response.
     *
     * @param request API request
     * @return API response
     */
    @Override
    public Map<String, Object> handleRequest(final ApiRequest request, final Context context) {
        if (request == null) {
            return buildUnsupportedOperationResponse(request);
        }

        logger.info("Consent History API service received request: {}", request);

        if (ApiHttpResource.SERVICE_USER_CONSENT_HISTORY.getValue().equals(request.resource())) {
            if (HttpMethod.GET.name().equals(request.httpMethod())) {
                final GetHistoryForServiceUserConsentActivity activity =
                    new GetHistoryForServiceUserConsentActivity(consentHistoryRepository);
                return new GetHistoryForServiceUserConsentRequestHandler(activity).handleRequest(request);
            }
        }

        return buildUnsupportedOperationResponse(request);
    }

    private Map<String, Object> buildUnsupportedOperationResponse(final ApiRequest request) {
        final String requestResource = request == null ? null : request.resource();
        final String requestHttpMethod = request == null ? null : request.httpMethod();
        final String errorMessage = String.format(UNSUPPORTED_OPERATION_MESSAGE, requestResource, requestHttpMethod);
        logger.warn(errorMessage);

        final Map<String, Object> apiErrorResponse = new HashMap<String, Object>();
        apiErrorResponse.put(ApiResponseParameterName.STATUS_CODE.getValue(), HttpStatusCode.BAD_REQUEST.getValue());
        apiErrorResponse.put(ApiResponseParameterName.BODY.getValue(), String.format(ApiRequestHandler.ERROR_RESPONSE_BODY, errorMessage));
        return apiErrorResponse;
    }
}
