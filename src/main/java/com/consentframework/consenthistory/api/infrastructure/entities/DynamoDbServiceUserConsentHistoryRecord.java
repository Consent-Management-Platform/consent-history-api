package com.consentframework.consenthistory.api.infrastructure.entities;

import com.consentframework.consenthistory.api.domain.entities.StoredConsent;
import com.consentframework.consenthistory.api.infrastructure.mappers.DynamoDbConsentConverter;
import com.consentframework.shared.api.infrastructure.annotations.DynamoDbImmutableStyle;
import jakarta.annotation.Nullable;
import org.immutables.value.Value.Immutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * DynamoDB data class representing a service user consent history record.
 */
@Immutable
@DynamoDbImmutableStyle
@DynamoDbImmutable(builder = DynamoDbServiceUserConsentHistoryRecord.Builder.class)
public interface DynamoDbServiceUserConsentHistoryRecord {
    public static final String TABLE_NAME = "ConsentHistory";
    public static final String PARTITION_KEY = "id";
    public static final String SORT_KEY = "eventId";

    static Builder builder() {
        return new Builder();
    }

    /**
     * DynamoDbServiceUserConsentHistoryRecord Builder class, intentionally empty.
     */
    class Builder extends ImmutableDynamoDbServiceUserConsentHistoryRecord.Builder {}

    @DynamoDbPartitionKey
    String id();

    @DynamoDbSortKey
    String eventId();

    String eventTime();

    String eventType();

    @DynamoDbConvertedBy(DynamoDbConsentConverter.class)
    @Nullable
    StoredConsent oldImage();

    @DynamoDbConvertedBy(DynamoDbConsentConverter.class)
    @Nullable
    StoredConsent newImage();
}
