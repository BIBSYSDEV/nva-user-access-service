package no.unit.nva.handlers;

import static nva.commons.utils.JsonUtils.objectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.exceptions.InvalidUserInternalException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;

public final class UserDtoCreator {

    public static final String SOME_USERNAME = "SomeUsername";
    public static final String SOME_ROLENAME = "SomeRole";
    public static final String SOME_INSTITUTION = "SomeInstitution";

    /**
     * Creates a request for adding a user without a username. To be used with {@code handleRequest()} method.
     *
     * @return an InputStream.
     * @throws JsonProcessingException      if JSON serialization fails.
     * @throws InvalidUserInternalException unlikely. The object is intentionally invalid.
     * @throws InvalidRoleInternalException when role is invalid.
     * @throws NoSuchMethodException        reflection related.
     * @throws IllegalAccessException       reflection related.
     * @throws InvocationTargetException    reflection related.
     */
    public static InputStream createRequestWithUserWithoutUsername()
        throws JsonProcessingException, InvalidUserInternalException, InvalidRoleInternalException,
               NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        UserDto userWithoutUsername = createUserWithoutUsername();
        return new HandlerRequestBuilder<UserDto>(objectMapper)
            .withBody(userWithoutUsername)
            .build();
    }

    /**
     * Creates a user without a username. For testing output on invalid input.
     *
     * @return a {@link UserDto}
     * @throws InvalidRoleInternalException when the added role is invalid.
     * @throws InvalidUserInternalException unlikely.  The object is intentionally invalid.
     * @throws NoSuchMethodException        reflection related.
     * @throws InvocationTargetException    reflection related.
     * @throws IllegalAccessException       reflection related.
     */
    public static UserDto createUserWithoutUsername()
        throws InvalidRoleInternalException, InvalidUserInternalException, NoSuchMethodException,
               InvocationTargetException, IllegalAccessException {
        UserDto userWithoutUsername = createUserWithRolesAndInstitution();
        Method method = userWithoutUsername.getClass().getDeclaredMethod("setUsername", String.class);
        method.setAccessible(true);
        method.invoke(userWithoutUsername, "");

        return userWithoutUsername;
    }

    /**
     * create user without roles.
     *
     * @return {@link UserDto}
     * @throws InvalidUserInternalException When the user is invalid. The user is supposed to be a valid user
     */
    public static UserDto createUserWithoutRoles() throws InvalidUserInternalException {
        return UserDto.newBuilder().withUsername(SOME_USERNAME).build();
    }

    /**
     * Intention is to create a user with all fields filled.
     *
     * @throws InvalidUserInternalException When the user is invalid. The user is supposed to be a valid user.
     * @throws InvalidRoleInternalException When the user roles is invalid. The role is supposed to be a valid role.
     */
    public static UserDto createUserWithRolesAndInstitution()
        throws InvalidUserInternalException, InvalidRoleInternalException {
        return createUserWithRoleWithoutInstitution().copy()
            .withInstitution(SOME_INSTITUTION)
            .build();
    }

    /**
     * Creates a a user with username and a role but without institution.
     *
     * @return {@link UserDto}
     * @throws InvalidUserInternalException When the user is invalid. The user is supposed to be a valid user.
     * @throws InvalidRoleInternalException When the role is invalid. The role is supposed to be a valid role.
     */
    public static UserDto createUserWithRoleWithoutInstitution()
        throws InvalidUserInternalException, InvalidRoleInternalException {
        RoleDto sampleRole = RoleDto.newBuilder().withName(SOME_ROLENAME).build();
        return UserDto.newBuilder()
            .withUsername(SOME_USERNAME)
            .withRoles(Collections.singletonList(sampleRole))
            .build();
    }
}
