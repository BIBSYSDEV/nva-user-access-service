package no.unit.nva.model;

import static java.util.Objects.isNull;
import static nva.commons.utils.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import no.unit.nva.database.AccessRight;
import no.unit.nva.database.RoleDb;
import no.unit.nva.database.interfaces.WithCopy;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.model.RoleDto.Builder;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;

@JsonTypeName(RoleDto.TYPE)
public class RoleDto implements WithCopy<Builder>, JsonSerializable, Validable, Typed {

    public static final String TYPE = "Role";
    public static final String MISSING_ROLE_NAME_ERROR = "Role should have a name";
    @JsonProperty("rolename")
    private String roleName;
    @JsonProperty("accessRights")
    private List<AccessRight> accessRights;

    public RoleDto() {
        accessRights = Collections.emptyList();
    }

    private RoleDto(Builder builder) throws InvalidEntryInternalException {
        this();
        setRoleName(builder.roleName);
        setAccessRights(builder.accessRights);
        if (!isValid()) {
            throw new InvalidEntryInternalException(MISSING_ROLE_NAME_ERROR);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a DTO from a DAO.
     *
     * @param roleDb the DAO
     * @return the DTO
     * @throws InvalidEntryInternalException when the input is not valid.
     */
    public static RoleDto fromRoleDb(RoleDb roleDb) throws InvalidEntryInternalException {
        return attempt(() -> newBuilder()
            .withName(roleDb.getName())
            .withAccessRights(roleDb.getAccessRights())
            .build())
            .orElseThrow(fail -> new InvalidEntryInternalException(fail.getException()));
    }

    @Override
    public Builder copy() {
        return new Builder()
            .withAccessRights(this.getAccessRights())
            .withName(this.getRoleName());
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {

        this.roleName = roleName;
    }

    public List<AccessRight> getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(List<AccessRight> accessRights) {
        this.accessRights = accessRights;
    }

    public RoleDb toRoleDb() throws InvalidEntryInternalException {
        return RoleDb.newBuilder().withName(this.roleName).withAccessRights(accessRights).build();
    }

    @Override
    public String toString() {
        return toJsonString(JsonUtils.objectMapper);
    }

    @Override
    public boolean isValid() {
        return !(isNull(this.getRoleName()) || this.getRoleName().isBlank());
    }

    @Override
    public InvalidInputException exceptionWhenInvalid() {
        return new InvalidInputException(MISSING_ROLE_NAME_ERROR);
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
        RoleDto roleDto = (RoleDto) o;
        return Objects.equals(getRoleName(), roleDto.getRoleName())
            && Objects.equals(getAccessRights(), roleDto.getAccessRights());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getRoleName(), getAccessRights());
    }

    public static final class Builder {

        private String roleName;
        private List<AccessRight> accessRights;

        private Builder() {
            this.accessRights = Collections.emptyList();
        }

        public Builder withName(String roleName) {
            this.roleName = roleName;
            return this;
        }

        public Builder withAccessRights(List<AccessRight> accessRights) {
            this.accessRights = accessRights;
            return this;
        }

        public RoleDto build() throws InvalidEntryInternalException {
            return new RoleDto(this);
        }
    }
}
