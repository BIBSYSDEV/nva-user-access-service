package no.unit.nva.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.model.RoleDto.Builder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class RoleDtoTest {

    public static final String SOME_ROLE_NAME = "someRoleName";

    @Test
    public void roleDtoShouldHaveABuilder() {
        Builder builder = RoleDto.newBuilder();
        assertNotNull(builder);
    }

    @Test
    public void builderShouldAllowSettingRoleName() throws InvalidRoleException {
        RoleDto role = RoleDto.newBuilder().withName(SOME_ROLE_NAME).build();
        assertThat(role.getName(), is(equalTo(SOME_ROLE_NAME)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " "})
    public void builderShouldNotAllowEmptyRoleName(String rolename) {
        Executable action = () -> RoleDto.newBuilder().withName(rolename).build();
        assertThrows(InvalidRoleException.class, action);
    }
}