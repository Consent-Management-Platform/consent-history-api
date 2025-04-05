package com.consentframework.consenthistory.api.testcommon.constants;

import com.consentframework.consenthistory.api.domain.constants.ApiPathParameterName;
import com.consentframework.consenthistory.api.domain.entities.StoredConsent;
import com.consentframework.consenthistory.api.models.Consent;
import com.consentframework.consenthistory.api.models.ConsentStatus;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class defining common test constants.
 */
public final class TestConstants {
    public static final String TEST_CONSENT_ID = "TestConsentId";
    public static final String TEST_SERVICE_ID = "TestServiceId";
    public static final String TEST_USER_ID = "TestUserId";
    public static final String TEST_PARTITION_KEY = String.format("%s|%s|%s", TEST_SERVICE_ID, TEST_USER_ID, TEST_CONSENT_ID);

    public static final ConsentStatus TEST_CONSENT_STATUS = ConsentStatus.ACTIVE;
    public static final String TEST_CONSENT_TYPE = "TestConsentType";
    public static final Map<String, String> TEST_CONSENT_DATA = Map.of(
        "testKey1", "testValue1",
        "testKey2", "testValue2"
    );
    public static final Map<String, AttributeValue> TEST_DDB_CONSENT_DATA = TEST_CONSENT_DATA.entrySet()
        .stream()
        .collect(
            Collectors.toMap(Map.Entry::getKey, e -> AttributeValue.builder().s(e.getValue()).build())
        );

    public static final String TEST_EVENT_TIME = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toString();

    public static final String TEST_CONSENT_HISTORY_PATH = String.format(
        "/v1/consent-history/services/%s/users/%s/consents/%s",
        TEST_SERVICE_ID, TEST_USER_ID, TEST_CONSENT_ID
    );
    public static final Map<String, String> TEST_CONSENT_HISTORY_PATH_PARAMS = Map.of(
        ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
        ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID,
        ApiPathParameterName.CONSENT_ID.getValue(), TestConstants.TEST_CONSENT_ID
    );

    public static final Consent TEST_CONSENT = new Consent()
        .serviceId(TEST_SERVICE_ID)
        .userId(TEST_USER_ID)
        .consentId(TEST_CONSENT_ID)
        .consentVersion(1)
        .status(TEST_CONSENT_STATUS)
        .consentType(TEST_CONSENT_TYPE)
        .consentData(TEST_CONSENT_DATA);

    public static final StoredConsent TEST_STORED_CONSENT = new StoredConsent()
        .id(TEST_PARTITION_KEY)
        .serviceId(TEST_SERVICE_ID)
        .userId(TEST_USER_ID)
        .consentId(TEST_CONSENT_ID)
        .consentVersion(1)
        .consentStatus(TEST_CONSENT_STATUS)
        .consentType(TEST_CONSENT_TYPE)
        .consentData(TEST_CONSENT_DATA);
}
