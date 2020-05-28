package no.unit.nva.database;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Collections;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseServiceTest extends DatabaseTest  {


    private DatabaseService db = new DatabaseService();

    @BeforeEach
    public void init(){
        db =new DatabaseService(initializeDatabase());
    }

    @Test
    public void databaseServiceTestShouldInsertValidItemInDatabase() {

        UserDto user = new UserDto();
        user.setUsername("ogk@unit.no");
        user.setInstitution("UNIT");
        RoleDto roleDto= new RoleDto.Builder().withName("CREATOR").build();
        user.setRoles(Collections.singletonList(roleDto));
        db.addUser(user);
        UserDto savedUsr= db.getUser("ogk@unit.no");
        assertThat(savedUsr.getUsername(), is(equalTo("ogk@unit.no")));
    }


}
