package no.unit.nva.model;

import java.util.Objects;
import no.unit.nva.database.RoleDb;
import no.unit.nva.database.exceptions.InvalidRoleException;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.StringUtils;

public class RoleDto {

    public static final String MISSING_ROLE_NAME_ERROR = "Role should have a name";
    public String name;

    private RoleDto(Builder builder) {
        setName(builder.name);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static RoleDto fromRoleDb(RoleDb roleDb) throws InvalidRoleException {
        return newBuilder().withName(roleDb.getName()).build();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return getName().equals(roleDto.getName());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getName());
    }

    public RoleDb toRoleDb() throws InvalidRoleException {
        return RoleDb.newBuilder().withName(this.name).build();
    }

    public static final class Builder {

        private String name;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public RoleDto build() throws InvalidRoleException {
            if (StringUtils.isEmpty(this.name)) {
                throw new InvalidRoleException(MISSING_ROLE_NAME_ERROR);
            }
            return new RoleDto(this);
        }
    }
}
