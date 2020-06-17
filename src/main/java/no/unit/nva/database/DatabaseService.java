package no.unit.nva.database;

import java.util.Optional;
import no.unit.nva.database.exceptions.InvalidInputRoleException;
import no.unit.nva.database.exceptions.InvalidRoleInternalException;
import no.unit.nva.database.exceptions.InvalidUserInternalException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;

public interface DatabaseService {

    String TABLE_NAME = "UsersRoles";

    Optional<UserDto> getUser(String username) throws InvalidUserInternalException;

    void addUser(UserDto user) throws InvalidUserInternalException;

    void addRole(RoleDto roleDto) throws InvalidRoleInternalException, InvalidInputRoleException;

    UserDto updateUser(UserDto user);

    Optional<RoleDto> getRole(RoleDto input) throws InvalidRoleInternalException;
}
