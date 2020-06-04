package no.unit.nva.database;

import java.util.Optional;
import no.unit.nva.database.exceptions.InvalidUserException;
import no.unit.nva.model.UserDto;

public interface DatabaseService {

    String TABLE_NAME= "UsersRoles";

    Optional<UserDto> getUser(String username) throws InvalidUserException;

    void addUser(UserDto user) throws InvalidUserException;

    UserDto updateUser(UserDto user);
}
