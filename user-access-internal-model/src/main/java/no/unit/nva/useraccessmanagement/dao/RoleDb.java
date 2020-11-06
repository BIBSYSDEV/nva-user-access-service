package no.unit.nva.useraccessmanagement.dao;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Objects;

import java.util.Set;
import no.unit.nva.useraccessmanagement.constants.DatabaseIndexDetails;
import no.unit.nva.useraccessmanagement.dao.RoleDb.Builder;
import no.unit.nva.useraccessmanagement.interfaces.WithCopy;
import no.unit.nva.useraccessmanagement.interfaces.WithType;
import no.unit.nva.useraccessmanagement.exceptions.InvalidEntryInternalException;
import nva.commons.utils.JacocoGenerated;

public class RoleDb extends DynamoEntryWithRangeKey implements WithCopy<Builder>, WithType {

    public static String TYPE = "ROLE";
    public static final String INVALID_PRIMARY_HASH_KEY = "PrimaryHashKey should start with \"" + TYPE + "\"";
    public static final String INVALID_PRIMARY_RANGE_KEY = "PrimaryHashKey should start with \"" + TYPE + "\"";

    @JsonProperty(DatabaseIndexDetails.PRIMARY_KEY_HASH_KEY)
    private String primaryHashKey;
    @JsonProperty(DatabaseIndexDetails.PRIMARY_KEY_RANGE_KEY)
    private String primaryRangeKey;
    @JsonProperty("accessRights")
    private Set<AccessRight> accessRights;

    @JsonProperty("name")
    private String name;

    public RoleDb() {
        super();
        this.accessRights = Collections.emptySet();
    }

    private RoleDb(Builder builder) throws InvalidEntryInternalException {
        super();
        setPrimaryHashKey(builder.primaryHashKey);
        setName(builder.name);
        setPrimaryRangeKey(builder.primaryRangeKey);
        setAccessRights(builder.accessRights);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JacocoGenerated
    @Override
    public String getPrimaryHashKey() {
        return this.primaryHashKey;
    }

    /**
     * Do not use this method. This is only for usage by the DynamoDbMapper. Sets the hash key value for the database
     * entry. This is the hashKey for the table and not any secondary index.
     *
     * @param primaryHashKey the primary hash key saved in the database
     * @throws InvalidEntryInternalException when the role is invalid.
     */
    @JacocoGenerated
    @Override
    public void setPrimaryHashKey(String primaryHashKey) throws InvalidEntryInternalException {
        if (primaryHashKeyHasNotBeenSet()) {
            if (!primaryHashKey.startsWith(TYPE)) {
                throw new InvalidEntryInternalException(INVALID_PRIMARY_HASH_KEY);
            }
            this.primaryHashKey = primaryHashKey;
        }
    }

    @Override
    public String getPrimaryRangeKey() {
        return this.primaryRangeKey;
    }

    @Override
    public void setPrimaryRangeKey(String primaryRangeKey) throws InvalidEntryInternalException {
        if (primaryRangeKeyHasNotBeenSet()) {
            if (!primaryRangeKey.startsWith(TYPE)) {
                throw new InvalidEntryInternalException(INVALID_PRIMARY_RANGE_KEY);
            }
            this.primaryRangeKey = primaryRangeKey;
        }
    }

    @JacocoGenerated
    public String getName() {
        return name;
    }

    @JacocoGenerated
    public void setName(String name) {
        this.name = name;
    }

    @JacocoGenerated
    @JsonProperty("type")
    @Override
    public String getType() {
        return TYPE;
    }

    public Set<AccessRight> getAccessRights() {
        return this.accessRights;
    }

    public void setAccessRights(Set<AccessRight> accessRights) {
        this.accessRights = accessRights;
    }

    @Override
    public Builder copy() {
        return new Builder()
            .withName(this.getName())
            .withAccessRights(this.getAccessRights());
    }

    @JacocoGenerated
    @Override
    public String toString() {
        return this.toJsonString();
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleDb roleDb = (RoleDb) o;
        return Objects.equals(getPrimaryHashKey(), roleDb.getPrimaryHashKey())
            && Objects.equals(getPrimaryRangeKey(), roleDb.getPrimaryRangeKey())
            && Objects.equals(getAccessRights(), roleDb.getAccessRights())
            && Objects.equals(getName(), roleDb.getName());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getPrimaryHashKey(), getPrimaryRangeKey(), getAccessRights(), getName());
    }

    public static final class Builder {

        public static final String EMPTY_ROLE_NAME_ERROR = "Rolename cannot be null or blank";
        private String name;
        private String primaryHashKey;
        private String primaryRangeKey;
        private Set<AccessRight> accessRights;

        private Builder() {
            accessRights = Collections.emptySet();
        }

        public Builder withName(String val) {
            name = val;
            return this;
        }

        public Builder withAccessRights(Set<AccessRight> accessRights) {
            this.accessRights = nonNull(accessRights) ? accessRights : Collections.emptySet();
            return this;
        }

        public RoleDb build() throws InvalidEntryInternalException {
            this.primaryHashKey = formatPrimaryHashKey();
            this.primaryRangeKey = formatPrimaryRangeKey();
            return new RoleDb(this);
        }

        private String formatPrimaryRangeKey() throws InvalidEntryInternalException {
            return this.formatPrimaryHashKey();
        }

        private String formatPrimaryHashKey() throws InvalidEntryInternalException {
            if (isNull(name) || name.isBlank()) {
                throw new InvalidEntryInternalException(EMPTY_ROLE_NAME_ERROR);
            } else {
                return String.join(DynamoEntryWithRangeKey.FIELD_DELIMITER, TYPE, name);
            }
        }
    }
}
