package no.unit.nva.database;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.exceptions.InvalidAccessRightException.InvalidAccessRightException;

@DynamoDBTyped(DynamoDBAttributeType.S)
public enum AccessRight {

    APPROVE_DOI_REQUEST;

    private static final Map<String, AccessRight> index = createIndex();

    @JsonCreator
    public static AccessRight fromString(String accessRight) {

        String stringValue = accessRight.toLowerCase(Locale.getDefault());
        if (index.containsKey(stringValue)) {
            return index.get(accessRight);
        } else {
            throw new InvalidAccessRightException(accessRight);
        }
    }

    @Override
    @JsonValue
    public String toString() {
        return this.name().toLowerCase(Locale.getDefault());
    }

    private static Map<String, AccessRight> createIndex() {
        return Arrays.stream(AccessRight.values())
            .collect(Collectors.toMap(AccessRight::toString, v -> v));
    }
}
