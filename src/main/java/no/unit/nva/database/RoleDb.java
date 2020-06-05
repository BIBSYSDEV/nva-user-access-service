package no.unit.nva.database;

import static java.util.Objects.isNull;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.Objects;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.database.intefaces.DynamoEntry;
import no.unit.nva.database.intefaces.WithCopy;
import no.unit.nva.database.intefaces.WithType;
import nva.commons.utils.JacocoGenerated;

@DynamoDBTable(tableName = "UsersRoles")
public class RoleDb implements WithCopy<RoleDb.Builder>, WithType, DynamoEntry {

    private static final String INVALID_PRIMARY_HASH_KEY = "PrimaryHashKey should start with \"ROLE\"";
    public static String TYPE = "ROLE";

    private String primaryHashKey;
    private String name;

    public RoleDb() {
    }

    private RoleDb(Builder builder) throws InvalidRoleException {
        setName(builder.name);
        setPrimaryHashKey(builder.primaryHashKey);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JacocoGenerated
    @DynamoDBHashKey(attributeName = "PK1A")
    public String getPrimaryHashKey() {
        return this.primaryHashKey;
    }

    @DynamoDBRangeKey(attributeName = "PK1B")
    public String getPrimaryRangeKey() {
        return getType();
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "type")
    @Override
    public String getType() {
        return TYPE;
    }

    @JacocoGenerated
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Do not use this method. This is only for usage by the DynamoDbMapper. Sets the hash key value for the database
     * entry. This is the hashKey for the table and not any secondary index.
     *
     * @param primaryHashKey the primary hash key saved in the database
     * @throws InvalidRoleException when the role is invalid.
     */
    @JacocoGenerated
    public void setPrimaryHashKey(String primaryHashKey) throws InvalidRoleException {
        if (primaryHashKeyHasNotBeenSet()) {
            if (!primaryHashKey.startsWith(TYPE)) {
                throw new InvalidRoleException(INVALID_PRIMARY_HASH_KEY);
            }
            this.primaryHashKey = primaryHashKey;
        }
    }

    private boolean primaryHashKeyHasNotBeenSet() {
        return isNull(this.primaryHashKey);
    }

    @JacocoGenerated
    public void setPrimaryRangeKey(String primaryRangeKey) {
        // DO NOTHING.
    }

    /**
     * Do not use. Intented only for use from DynamoDB. This method has no effect as the type is always ROLE.
     *
     * @param type ignored parameter.
     */
    @JacocoGenerated
    public void setType(String type) {
        // DO NOTHING
    }

    @Override
    public Builder copy() {
        Builder builder = new Builder()
            .withName(this.name);
        return builder;
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
        return getName().equals(roleDb.getName());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getName());
    }

    public static final class Builder {

        public static final String EMPTY_ROLE_NAME_ERROR = "Rolename cannot be null or blank";
        private String name;
        private String primaryHashKey;

        private Builder() {
        }

        public Builder withName(String val) {
            name = val;
            return this;
        }

        private String formatPrimaryHashKey() throws InvalidRoleException {
            if (isNull(name) || name.isBlank()) {
                throw new InvalidRoleException(EMPTY_ROLE_NAME_ERROR);
            } else {
                return String.join(FIELD_DELIMITER, TYPE, name);
            }
        }

        public RoleDb build() throws InvalidRoleException {
            this.primaryHashKey = formatPrimaryHashKey();
            return new RoleDb(this);
        }
    }
}
