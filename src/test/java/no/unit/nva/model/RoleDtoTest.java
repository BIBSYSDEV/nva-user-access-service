package no.unit.nva.model;

import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import no.unit.nva.database.exceptions.InvalidRoleInternalException;
import no.unit.nva.model.RoleDto.Builder;
import nva.commons.utils.attempt.Failure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class RoleDtoTest {

    public static final String SOME_ROLE_NAME = "someRoleName";
    public static final String SOME_MESSAGE = "Some message";

    @Test
    public void roleDtoShouldHaveABuilder() {
        Builder builder = RoleDto.newBuilder();
        assertNotNull(builder);
    }

    @Test
    public void builderShouldAllowSettingRoleName() throws InvalidRoleInternalException {
        RoleDto role = RoleDto.newBuilder().withName(SOME_ROLE_NAME).build();
        assertThat(role.getRoleName(), is(equalTo(SOME_ROLE_NAME)));
    }

    @ParameterizedTest(name = "builder should throw exception when rolename is:\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"", " "})
    public void builderShouldNotAllowEmptyRoleName(String rolename) {
        Executable action = () -> RoleDto.newBuilder().withName(rolename).build();
        assertThrows(InvalidRoleInternalException.class, action);
    }

    @Test
    public void toStringReturnsStringContainingTheNameOfTheRole() throws InvalidRoleInternalException {
        RoleDto role = RoleDto.newBuilder().withName(SOME_ROLE_NAME).build();
        assertThat(role.toString(), containsString(role.getRoleName()));
    }

    @Test
    public void unexpectedExceptionThrowsRuntimeException() throws NoSuchMethodException {
        RoleDto role = new RoleDto();
        Method method = role.getClass().getDeclaredMethod("newUnexpectedException", Failure.class);
        method.setAccessible(true);
        Exception exceptionDuringSerialization = new Exception(SOME_MESSAGE);
        Failure<Object> failure = new Failure<>(exceptionDuringSerialization);
        Executable action = () -> method.invoke(role, failure);
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, action);
        assertThat(thrown.getCause().getClass(), is(equalTo(RuntimeException.class)));
    }

    @Test
    public void copyReturnsABuilderWithAllFieldsOfOriginalObjectPreserved() throws InvalidRoleInternalException {
        RoleDto original = RoleDto.newBuilder().withName(SOME_ROLE_NAME).build();
        RoleDto copy = original.copy().build();
        assertThat(original, doesNotHaveNullOrEmptyFields());
        assertThat(copy, is(not(sameInstance(original))));
        assertThat(copy, is(equalTo(original)));
    }
}