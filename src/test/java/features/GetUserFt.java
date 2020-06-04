package features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Collections;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.DatabaseTest;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.database.exceptions.InvalidUserException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import org.junit.jupiter.api.BeforeEach;

public class GetUserFt extends DatabaseTest {

    private String username;

    //TODO: this wll be a handler
    private DatabaseServiceImpl db = new DatabaseServiceImpl();
    private UserDto user;

    @BeforeEach
    public void init() {
        db = new DatabaseServiceImpl(initializeDatabase());
    }

    public GetUserFt() {
    }

    @Given("a user with username {string}")
    public void a_user_with_username(String username) throws InvalidUserException, InvalidRoleException {
        RoleDto role = RoleDto.newBuilder().withName("CREATOR").build();
        UserDto userDto = UserDto.newBuilder()
            .withUsername(username)
            .withInstitution("NTNU")
            .withRoles(Collections.singletonList(role))
            .build();

        db.addUser(userDto);
        this.username = username;
    }

    @When("handler receives a request for accessing the user details")
    public void handler_receives_a_request_for_accessing_user_details() throws InvalidUserException {
        this.user = db.getUser(username).get();
    }

    @Then("the handler returns the user details")
    public void the_handler_returns_the_user_details() {
        assertThat(user.getUsername(), is(equalTo(this.username)));
    }
}
