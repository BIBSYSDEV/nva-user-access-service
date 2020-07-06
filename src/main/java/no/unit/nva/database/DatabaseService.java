package no.unit.nva.database;

import java.util.Optional;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidInputRoleException;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.exceptions.InvalidUserInternalException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;

public interface DatabaseService {

    String USERS_AND_ROLES_TABLE_NAME_ENV_VARIABLE = "USERS_AND_ROLES_TABLE";

    Optional<UserDto> getUser(UserDto queryObject) throws InvalidUserInternalException;

    void addUser(UserDto user) throws InvalidUserInternalException, ConflictException;

    void addRole(RoleDto roleDto) throws InvalidRoleInternalException, InvalidInputRoleException;

    UserDto updateUser(UserDto user);

    Optional<RoleDto> getRole(RoleDto input) throws InvalidRoleInternalException;
}
