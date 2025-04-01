package com.consentframework.consenthistory.api.testcommon.constants;

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
}
