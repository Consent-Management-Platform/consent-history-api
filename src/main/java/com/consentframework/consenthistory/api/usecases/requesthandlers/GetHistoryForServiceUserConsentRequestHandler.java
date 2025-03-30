package com.consentframework.consenthistory.api.usecases.requesthandlers;

import com.consentframework.consenthistory.api.domain.constants.ApiPathParameterName;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.shared.api.domain.requesthandlers.ApiRequestHandler;

import java.util.Map;

/**
 * Handles requests to retrieve history for a given service user consent.
 */
public class GetHistoryForServiceUserConsentRequestHandler extends ApiRequestHandler {

    /**
     * Request handler constructor.
     */
    public GetHistoryForServiceUserConsentRequestHandler() {
        super(ApiPathParameterName.CONSENT_PATH_PARAM_NAMES);
    }

    @Override
    protected Map<String, Object> handleRequest(final ApiRequest request) {
        // TODO: implement logic to retrieve consent history
        return logAndBuildErrorResponse(new ResourceNotFoundException("No consent history found"));
    }
}
