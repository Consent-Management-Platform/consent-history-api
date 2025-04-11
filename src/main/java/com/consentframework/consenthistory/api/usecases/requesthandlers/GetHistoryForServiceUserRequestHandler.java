package com.consentframework.consenthistory.api.usecases.requesthandlers;

import com.consentframework.consenthistory.api.JSON;
import com.consentframework.consenthistory.api.domain.constants.ApiPathParameterName;
import com.consentframework.consenthistory.api.models.ConsentHistory;
import com.consentframework.consenthistory.api.models.GetHistoryForServiceUserResponseContent;
import com.consentframework.consenthistory.api.usecases.activities.GetHistoryForServiceUserActivity;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.exceptions.InternalServiceException;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.shared.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.shared.api.domain.requesthandlers.ApiRequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Handles requests to retrieve history for a given service user.
 */
public class GetHistoryForServiceUserRequestHandler extends ApiRequestHandler {
    private static final Logger logger = LogManager.getLogger(GetHistoryForServiceUserRequestHandler.class);
    private static final ObjectMapper objectMapper = new JSON().getMapper();

    final GetHistoryForServiceUserActivity activity;

    /**
     * Request handler constructor.
     */
    public GetHistoryForServiceUserRequestHandler(final GetHistoryForServiceUserActivity activity) {
        super(ApiPathParameterName.CONSENTS_PATH_PARAM_NAMES);
        this.activity = activity;
    }

    /**
     * Handle GetHistoryForServiceUser API requests.
     *
     * @param request API request
     * @return API response
     */
    @Override
    public Map<String, Object> handleRequest(final ApiRequest request) {
        final String serviceId;
        final String userId;
        try {
            serviceId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.SERVICE_ID.getValue());
            userId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.USER_ID.getValue());
        } catch (final BadRequestException badRequestException) {
            return logAndBuildMissingPathParamResponse(badRequestException);
        }

        final GetHistoryForServiceUserResponseContent activityResponse;
        final String responseBodyString;
        try {
            activityResponse = activity.handleRequest(serviceId, userId);
            final List<ConsentHistory> consentHistory = activityResponse.getData();
            final GetHistoryForServiceUserResponseContent responseContent = new GetHistoryForServiceUserResponseContent()
                .data(consentHistory);
            responseBodyString = toJsonString(objectMapper, responseContent);
        } catch (final InternalServiceException | JsonProcessingException | ResourceNotFoundException exception) {
            return logAndBuildErrorResponse(exception);
        }

        logger.info("Successfully retrieved {} consent histories for path {}",
            activityResponse.getData().size(), request.path());
        return buildApiSuccessResponse(responseBodyString);
    }
}
