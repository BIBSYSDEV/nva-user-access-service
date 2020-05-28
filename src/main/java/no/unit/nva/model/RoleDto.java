package no.unit.nva.model;

import java.util.Objects;
import nva.commons.utils.JacocoGenerated;

public class RoleDto {

    public String name;

    private RoleDto(Builder builder) {
        setName(builder.name);
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

    public static final class Builder {

        private String name;

        public Builder() {
        }

        public Builder withName(String val) {
            name = val;
            return this;
        }

        public RoleDto build() {
            return new RoleDto(this);
        }
    }
}
