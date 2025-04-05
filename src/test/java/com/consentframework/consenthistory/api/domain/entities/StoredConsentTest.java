package com.consentframework.consenthistory.api.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consentframework.consenthistory.api.models.ConsentStatus;
import com.consentframework.consenthistory.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;

class StoredConsentTest {
    @Test
    void equalsWhenNull() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;
        assertFalse(consent.equals(null));
    }

    @Test
    void equalsWhenSameObject() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;
        assertTrue(consent.equals(consent));
    }

    @Test
    void equalsWhenSameValues() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;
        final StoredConsent consentWithSameValues = cloneStoredConsent(consent);
        assertTrue(consent.equals(consentWithSameValues));
    }

    @Test
    void equalsWhenDifferentServiceId() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;

        final StoredConsent consentWithDifferentData = cloneStoredConsent(consent)
            .serviceId("differentServiceId");
        assertFalse(consent.equals(consentWithDifferentData));
    }

    @Test
    void equalsWhenDifferentUserId() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;

        final StoredConsent consentWithDifferentData = cloneStoredConsent(consent)
            .userId("differentUserId");
        assertFalse(consent.equals(consentWithDifferentData));
    }

    @Test
    void equalsWhenDifferentConsentId() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;

        final StoredConsent consentWithDifferentData = cloneStoredConsent(consent)
            .consentId("differentConsentId");
        assertFalse(consent.equals(consentWithDifferentData));
    }

    @Test
    void equalsWhenDifferentConsentVersion() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;

        final StoredConsent consentWithDifferentData = cloneStoredConsent(consent)
            .consentVersion(5);
        assertFalse(consent.equals(consentWithDifferentData));
    }

    @Test
    void equalsWhenDifferentConsentStatus() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;

        final StoredConsent consentWithDifferentData = cloneStoredConsent(consent)
            .consentStatus(ConsentStatus.EXPIRED);
        assertFalse(consent.equals(consentWithDifferentData));
    }

    @Test
    void equalsWhenDifferentConsentType() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;

        final StoredConsent consentWithDifferentData = cloneStoredConsent(consent)
            .consentType("differentConsentType");
        assertFalse(consent.equals(consentWithDifferentData));
    }

    @Test
    void equalsWhenDifferentExpiryTime() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;

        final StoredConsent consentWithDifferentData = cloneStoredConsent(consent)
            .expiryTime(OffsetDateTime.now());
        assertFalse(consent.equals(consentWithDifferentData));
    }

    @Test
    void equalsWhenDifferentData() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;

        final Map<String, String> differentConsentData = Map.of("key", "value");
        final StoredConsent consentWithDifferentData = cloneStoredConsent(consent)
            .consentData(differentConsentData);
        assertFalse(consent.equals(consentWithDifferentData));
    }

    @Test
    void hashCodeIsSameForEqualValues() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;
        final StoredConsent clonedConsent = cloneStoredConsent(consent);
        assertEquals(consent.hashCode(), clonedConsent.hashCode());
    }

    @Test
    void hashCodeIsDifferentForDifferentValues() {
        final StoredConsent consent = TestConstants.TEST_STORED_CONSENT;
        final StoredConsent clonedConsent = cloneStoredConsent(consent)
            .consentData(Map.of("differentKey", "differentValue"));
        assertNotEquals(consent.hashCode(), clonedConsent.hashCode());
    }

    private StoredConsent cloneStoredConsent(final StoredConsent originalConsent) {
        return new StoredConsent()
            .serviceId(originalConsent.getServiceId())
            .userId(originalConsent.getUserId())
            .consentId(originalConsent.getConsentId())
            .consentVersion(originalConsent.getConsentVersion())
            .consentStatus(originalConsent.getConsentStatus())
            .consentType(originalConsent.getConsentType())
            .consentData(originalConsent.getConsentData());
    }
}
