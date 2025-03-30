package com.consentframework.consenthistory.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.consentframework.shared.api.domain.constants.ApiResponseParameterName;
import com.consentframework.shared.api.domain.constants.HttpStatusCode;
import org.junit.jupiter.api.Test;

import java.util.Map;

class GetHistoryForServiceUserConsentRequestHandlerTest {
    private final GetHistoryForServiceUserConsentRequestHandler handler = new GetHistoryForServiceUserConsentRequestHandler();

    @Test
    void testHandleRequestWhenConsentNotFound() {
        final Map<String, Object> response = handler.handleRequest(null);
        assertNotNull(response);
        assertEquals(HttpStatusCode.NOT_FOUND.getValue(), response.get(ApiResponseParameterName.STATUS_CODE.getValue()));
    }
}
