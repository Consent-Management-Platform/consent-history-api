package com.consentframework.consenthistory.api.domain.constants;

/**
 * API HTTP resources, which represent REST resource paths with path parameter placeholders.
 */
public enum ApiHttpResource {
    SERVICE_USER_CONSENT_HISTORY("/v1/consent-history/services/{serviceId}/users/{userId}/consents/{consentId}"),
    SERVICE_USER_HISTORY("/v1/consent-history/services/{serviceId}/users/{userId}/consents/{consentId}");

    private final String value;

    private ApiHttpResource(final String value) {
        this.value = value;
    }

    /**
     * Return resource path.
     *
     * @return resource path
     */
    public String getValue() {
        return value;
    }
}
