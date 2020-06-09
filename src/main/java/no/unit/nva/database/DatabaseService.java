package no.unit.nva.database;

import java.util.Optional;
import no.unit.nva.database.exceptions.InvalidInputRoleException;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.database.exceptions.InvalidUserException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;

public interface DatabaseService {

    String TABLE_NAME = "UsersRoles";

    Optional<UserDto> getUser(String username) throws InvalidUserException;

    void addUser(UserDto user) throws InvalidUserException;

    void addRole(RoleDto roleDto) throws InvalidRoleException, InvalidInputRoleException;

    UserDto updateUser(UserDto user);

    Optional<RoleDto> getRole(RoleDto input) throws InvalidRoleException;
}
