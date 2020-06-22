package no.unit.nva.model;

import static nva.commons.utils.attempt.Try.attempt;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import no.unit.nva.database.RoleDb;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.database.intefaces.WithCopy;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.StringUtils;
import nva.commons.utils.attempt.Failure;

public class RoleDto implements WithCopy<RoleDto.Builder> {

    public static final String MISSING_ROLE_NAME_ERROR = "Role should have a name";
    @JsonProperty("rolename")
    private String roleName;

    public RoleDto() {
    }

    private RoleDto(Builder builder) {
        setRoleName(builder.name);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static RoleDto fromRoleDb(RoleDb roleDb) throws InvalidRoleInternalException {
        return newBuilder().withName(roleDb.getName()).build();
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
        return getRoleName().equals(roleDto.getRoleName());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getRoleName());
    }

    public RoleDb toRoleDb() throws InvalidRoleInternalException {
        return RoleDb.newBuilder().withName(this.roleName).build();
    }

    /**
     * a JSON representation of the object.
     *
     * @return a JSON representation of the object.
     */
    @Override
    public String toString() {
        return
            attempt(() -> JsonUtils.objectMapper.writeValueAsString(this))
                .orElseThrow(this::newUnexpectedException);
    }

    private RuntimeException newUnexpectedException(Failure<String> fail) {
        throw new RuntimeException(fail.getException());
    }

    @Override
    public RoleDto.Builder copy() {
        return RoleDto.newBuilder().withName(getRoleName());
    }

    public static final class Builder {

        private String name;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Builds a RoleDto.
         *
         * @return a RoleDto
         * @throws InvalidRoleInternalException when the generated role would be invalid.
         */
        public RoleDto build() throws InvalidRoleInternalException {
            if (StringUtils.isEmpty(this.name)) {
                throw new InvalidRoleInternalException(MISSING_ROLE_NAME_ERROR);
            }
            return new RoleDto(this);
        }
    }
}
