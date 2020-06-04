package features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Collections;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseTest;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import org.junit.jupiter.api.BeforeEach;


public class GetUserFt extends DatabaseTest {

    private String username;

    //TODO: this wll be a handler
    private DatabaseService db = new DatabaseService();
    private UserDto user;

    @BeforeEach
    public void init(){
        db =new DatabaseService(initializeDatabase());
    }

    public GetUserFt() {
    }

    @Given("a user with username {string}")
    public void a_user_with_username(String username) {
        UserDto userDto= new UserDto();
        userDto.setUsername(username);
        userDto.setInstitution("NTNU");
        RoleDto role = new RoleDto.Builder().withName("CREATOR").build();
        userDto.setRoles(Collections.singletonList(role));
        db.addUser(userDto);
        this.username = username;

    }

    @When("handler receives a request for accessing the user details")
    public void handler_receives_a_request_for_accessing_user_details() {
        this.user= db.getUser(username);
    }

    @Then("the handler returns the user details")
    public void the_handler_returns_the_user_details() {
        assertThat(user.getUsername(),is(equalTo(this.username)));
    }
}
