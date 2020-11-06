package no.unit.nva.useraccessmanagement.model;

import static java.util.Objects.isNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import no.unit.nva.useraccessmanagement.dao.AccessRight;
import no.unit.nva.useraccessmanagement.dao.RoleDb;
import no.unit.nva.useraccessmanagement.dao.exceptions.InvalidEntryInternalException;
import no.unit.nva.useraccessmanagement.dao.interfaces.JsonSerializable;
import no.unit.nva.useraccessmanagement.dao.interfaces.WithCopy;
import no.unit.nva.useraccessmanagement.model.RoleDto.Builder;
import no.unit.nva.useraccessmanagement.model.exceptions.InvalidInputException;
import no.unit.nva.useraccessmanagement.model.interfaces.Typed;
import no.unit.nva.useraccessmanagement.model.interfaces.Validable;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.attempt.Try;

@JsonTypeName(RoleDto.TYPE)
public class RoleDto implements WithCopy<Builder>, JsonSerializable, Validable, Typed {

    public static final String TYPE = "Role";
    public static final String MISSING_ROLE_NAME_ERROR = "Role should have a name";
    @JsonProperty("rolename")
    private String roleName;
    @JsonProperty("accessRights")
    private Set<AccessRight> accessRights;

    public RoleDto() {
        accessRights = Collections.emptySet();
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
        return Try.attempt(() -> newBuilder()
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

    public Set<AccessRight> getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(Set<AccessRight> accessRights) {
        this.accessRights = accessRights;
    }

    public RoleDb toRoleDb() throws InvalidEntryInternalException {
        return RoleDb.newBuilder().withName(this.roleName).withAccessRights(accessRights).build();
    }

    @Override
    @JacocoGenerated
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
        private Set<AccessRight> accessRights;

        private Builder() {
            this.accessRights = Collections.emptySet();
        }

        public Builder withName(String roleName) {
            this.roleName = roleName;
            return this;
        }

        public Builder withAccessRights(Set<AccessRight> accessRights) {
            this.accessRights = accessRights;
            return this;
        }

        public RoleDto build() throws InvalidEntryInternalException {
            return new RoleDto(this);
        }
    }
}
