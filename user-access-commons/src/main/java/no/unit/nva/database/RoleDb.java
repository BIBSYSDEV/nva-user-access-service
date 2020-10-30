package no.unit.nva.database;

import static java.util.Objects.isNull;
import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_HASH_KEY;
import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_RANGE_KEY;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.Objects;
import no.unit.nva.database.interfaces.DynamoEntryWithRangeKey;
import no.unit.nva.database.interfaces.WithCopy;
import no.unit.nva.database.interfaces.WithType;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import nva.commons.utils.JacocoGenerated;

@DynamoDBTable(tableName = "OverridenByEnvironmentVariable")
public class RoleDb extends DynamoEntryWithRangeKey implements WithCopy<RoleDb.Builder>, WithType {

    public static String TYPE = "ROLE";
    public static final String INVALID_PRIMARY_HASH_KEY = "PrimaryHashKey should start with \"" + TYPE + "\"";
    public static final String INVALID_PRIMARY_RANGE_KEY = "PrimaryHashKey should start with \"" + TYPE + "\"";

    private String primaryHashKey;
    private String name;
    private String primaryRangeKey;

    public RoleDb() {
        super();
    }

    private RoleDb(Builder builder) throws InvalidEntryInternalException {
        super();
        setName(builder.name);
        setPrimaryHashKey(builder.primaryHashKey);
        setPrimaryRangeKey(builder.primaryRangeKey);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JacocoGenerated
    @DynamoDBHashKey(attributeName = PRIMARY_KEY_HASH_KEY)
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
    public void setPrimaryHashKey(String primaryHashKey) throws InvalidEntryInternalException {
        if (primaryHashKeyHasNotBeenSet()) {
            if (!primaryHashKey.startsWith(TYPE)) {
                throw new InvalidEntryInternalException(INVALID_PRIMARY_HASH_KEY);
            }
            this.primaryHashKey = primaryHashKey;
        }
    }

    @DynamoDBRangeKey(attributeName = PRIMARY_KEY_RANGE_KEY)
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
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    @JacocoGenerated
    public void setName(String name) {
        this.name = name;
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "type")
    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Builder copy() {
        return new Builder().withName(this.name);
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
            && Objects.equals(getName(), roleDb.getName())
            && Objects.equals(getPrimaryRangeKey(), roleDb.getPrimaryRangeKey());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getPrimaryHashKey(), getName(), getPrimaryRangeKey());
    }

    public static final class Builder {

        public static final String EMPTY_ROLE_NAME_ERROR = "Rolename cannot be null or blank";
        private String name;
        private String primaryHashKey;
        private String primaryRangeKey;

        private Builder() {
        }

        public Builder withName(String val) {
            name = val;
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
                return String.join(FIELD_DELIMITER, TYPE, name);
            }
        }
    }
}
