package no.unit.nva.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.exceptions.InvalidAccessRightException;

public enum AccessRight {

    APPROVE_DOI_REQUEST,
    REJECT_DOI_REQUEST;

    private static final Map<String, AccessRight> index = createIndex();

    /**
     * Creates an AccessRight instance from a string (case insensitive).
     *
     * @param accessRight string representation of access right
     * @return an AccessRight instance.
     */
    @JsonCreator
    public static AccessRight fromString(String accessRight) {

        String lowerCased = accessRight.toLowerCase(Locale.getDefault());
        if (index.containsKey(lowerCased)) {
            return index.get(lowerCased);
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
