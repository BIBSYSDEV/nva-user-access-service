package no.unit.nva.database;

import static java.util.Objects.isNull;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import no.unit.nva.database.UserDb.Builder;
import no.unit.nva.database.intefaces.DynamoEntry;
import no.unit.nva.database.intefaces.WithCopy;
import no.unit.nva.exceptions.InvalidUserInternalException;
import nva.commons.utils.JacocoGenerated;

@DynamoDBTable(tableName = "UsersRoles")
@DynamoDBTyped
public class UserDb extends DynamoEntry implements WithCopy<Builder> {

    public static final String TYPE = "USER";
    public static final String INVALID_USER_EMPTY_USERNAME = "Invalid user entry: Empty username is not allowed";
    public static final String INVALID_PRIMARY_HASH_KEY = "PrimaryHashKey of user should start with \"USER\"";

    private String primaryHashKey;
    private String username;
    private String institution;
    private List<RoleDb> roles;

    public UserDb() {
        super();
    }

    private UserDb(Builder builder) throws InvalidUserInternalException {
        super();
        setUsername(builder.username);
        setInstitution(builder.institution);
        setRoles(builder.roles);
        setPrimaryHashKey(builder.primaryHashKey);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JacocoGenerated
    @DynamoDBHashKey(attributeName = "PK1A")
    @Override
    public String getPrimaryHashKey() {
        return this.primaryHashKey;
    }

    /**
     * Do not use this function. This function is defined only for internal usage (by DynamoDB). The function does not
     * reset the primaryKey once it has been set. It does not throw an Exception because this method is supposed ot be
     * used only by DynamoDb. For any other purpose use the {@link UserDb.Builder}
     *
     * @param primaryKey the primaryKey
     * @throws InvalidUserInternalException when the primary key is invalid.
     */
    public void setPrimaryHashKey(String primaryKey) throws InvalidUserInternalException {
        if (primaryKeyHasNotBeenSet()) {
            if (!primaryKey.startsWith(TYPE)) {
                throw new InvalidUserInternalException(INVALID_PRIMARY_HASH_KEY);
            }
            this.primaryHashKey = primaryKey;
        }
    }

    @JacocoGenerated
    @DynamoDBRangeKey(attributeName = "PK1B")
    @Override
    public String getPrimaryRangeKey() {
        return getType();
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "type")
    @Override
    public String getType() {
        return TYPE;
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "username")
    public String getUsername() {
        return username;
    }

    /**
     * Method for using only for DynamoDb mapper. Do not use. Use the builder instead.
     *
     * @param username the username of the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "roles")
    public List<RoleDb> getRoles() {
        return roles;
    }

    /**
     * Method for using only for DynamoDb mapper. Do not use. Use the builder instead.
     *
     * @param roles the roles.
     */
    public void setRoles(List<RoleDb> roles) {
        this.roles = roles;
    }

    @JacocoGenerated
    @DynamoDBAttribute(attributeName = "institution")
    public String getInstitution() {
        return institution;
    }

    /**
     * Method for using only for DynamoDb mapper. Do not use. Use the builder instead.
     *
     * @param institution the institution.
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    @Override
    public UserDb.Builder copy() {
        return newBuilder()
            .withUsername(this.username)
            .withInstitution(this.institution)
            .withRoles(this.roles);
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
        UserDb userDb = (UserDb) o;
        return Objects.equals(getPrimaryHashKey(), userDb.getPrimaryHashKey())
            && Objects.equals(getUsername(), userDb.getUsername())
            && Objects.equals(getInstitution(), userDb.getInstitution())
            && Objects.equals(getRoles(), userDb.getRoles());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getPrimaryHashKey(), getUsername(), getInstitution(), getRoles());
    }

    public static final class Builder {

        private String username;
        private String institution;
        private List<RoleDb> roles;
        private String primaryHashKey;

        private Builder() {
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withInstitution(String institution) {
            this.institution = institution;
            return this;
        }

        public Builder withRoles(Collection<RoleDb> roles) {
            this.roles = new ArrayList<>(roles);
            return this;
        }

        public UserDb build() throws InvalidUserInternalException {
            this.primaryHashKey = formatPrimaryHashKey();
            return new UserDb(this);
        }

        private String formatPrimaryHashKey() throws InvalidUserInternalException {
            if (isNull(username) || username.isBlank()) {
                throw new InvalidUserInternalException(INVALID_USER_EMPTY_USERNAME);
            } else {
                return String.join(FIELD_DELIMITER, TYPE, username);
            }
        }
    }
}
