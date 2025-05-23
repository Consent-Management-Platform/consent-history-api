package com.consentframework.consenthistory.api.domain.constants;

import java.util.List;

/**
 * API path parameter names.
 */
public enum ApiPathParameterName {
    SERVICE_ID("serviceId"),
    USER_ID("userId"),
    CONSENT_ID("consentId");

    private final String value;

    private ApiPathParameterName(final String value) {
        this.value = value;
    }

    /**
     * Return parameter name.
     *
     * @return parameter name
     */
    public String getValue() {
        return value;
    }

    // Required path parameters for /consents APIs
    public static final List<String> CONSENTS_PATH_PARAM_NAMES = List.of(
        SERVICE_ID.getValue(), USER_ID.getValue());

    // Required path parameters for /consents/{consentId} APIs, eg. get consent history
    public static final List<String> CONSENT_PATH_PARAM_NAMES = List.of(
        SERVICE_ID.getValue(), USER_ID.getValue(), CONSENT_ID.getValue());
}
