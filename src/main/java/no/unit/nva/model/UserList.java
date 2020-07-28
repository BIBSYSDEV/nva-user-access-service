package no.unit.nva.model;

import java.util.ArrayList;
import java.util.Collection;

public class UserList extends ArrayList<UserDto> {

    public UserList() {
        super();
    }

    protected UserList(Collection<? extends UserDto> c) {
        super(c);
    }

    public static UserList fromCollection(Collection<UserDto> users) {
        return new UserList(users);
    }

}
