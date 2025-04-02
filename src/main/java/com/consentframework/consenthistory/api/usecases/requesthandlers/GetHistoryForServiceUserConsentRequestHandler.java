package com.consentframework.consenthistory.api.usecases.requesthandlers;

import com.consentframework.consenthistory.api.JSON;
import com.consentframework.consenthistory.api.domain.constants.ApiPathParameterName;
import com.consentframework.consenthistory.api.domain.repositories.ServiceUserConsentHistoryRepository;
import com.consentframework.consenthistory.api.models.ConsentChangeEvent;
import com.consentframework.consenthistory.api.models.GetHistoryForServiceUserConsentResponseContent;
import com.consentframework.consenthistory.api.usecases.activities.GetHistoryForServiceUserConsentActivity;
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
 * Handles requests to retrieve history for a given service user consent.
 */
public class GetHistoryForServiceUserConsentRequestHandler extends ApiRequestHandler {
    private static final Logger logger = LogManager.getLogger(GetHistoryForServiceUserConsentRequestHandler.class);
    private static final ObjectMapper objectMapper = new JSON().getMapper();

    private final GetHistoryForServiceUserConsentActivity activity;

    /**
     * Request handler constructor.
     */
    public GetHistoryForServiceUserConsentRequestHandler(final GetHistoryForServiceUserConsentActivity activity) {
        super(ApiPathParameterName.CONSENT_PATH_PARAM_NAMES);
        this.activity = activity;
    }

    /**
     * Handle GetHistoryForServiceUserConsent API requests.
     *
     * @param request API request
     * @return API response
     */
    @Override
    public Map<String, Object> handleRequest(final ApiRequest request) {
        final String serviceId;
        final String userId;
        final String consentId;
        try {
            serviceId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.SERVICE_ID.getValue());
            userId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.USER_ID.getValue());
            consentId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.CONSENT_ID.getValue());
        } catch (final BadRequestException badRequestException) {
            return logAndBuildMissingPathParamResponse(badRequestException);
        }

        final GetHistoryForServiceUserConsentResponseContent activityResponse;
        final String responseBodyString;
        try {
            activityResponse = activity.handleRequest(serviceId, userId, consentId);
            final List<ConsentChangeEvent> consentHistory = activityResponse.getData();
            if (consentHistory == null || consentHistory.isEmpty()) {
                logger.info("GetHistoryForServiceUserConsentActivity.handleRequest response has no data: {}", activityResponse);
                final String errorMessage = String.format(ServiceUserConsentHistoryRepository.CONSENT_NOT_FOUND_MESSAGE,
                    serviceId, userId, consentId);
                return logAndBuildErrorResponse(new ResourceNotFoundException(errorMessage));
            }
            final GetHistoryForServiceUserConsentResponseContent responseContent = new GetHistoryForServiceUserConsentResponseContent()
                .data(consentHistory);
            responseBodyString = toJsonString(objectMapper, responseContent);
        } catch (final InternalServiceException | JsonProcessingException | ResourceNotFoundException exception) {
            logger.warn("GetHistoryForServiceUserConsentRequestHandler handling exception {}", exception);
            return logAndBuildErrorResponse(exception);
        }

        logger.info("Successfully retrieved {} consent history records for path {}",
            activityResponse.getData().size(), request.path());
        return buildApiSuccessResponse(responseBodyString);
    }
}
