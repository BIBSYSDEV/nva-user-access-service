package features;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.io.IOException;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.handlers.GetUserHandler;
import no.unit.nva.model.UserDto;

public class GetUserFt extends ScenarioTest {

    public GetUserFt(ScenarioContext scenarioContext) {
        super(scenarioContext);
    }

    @Given("^that a user entry with the username (.*) exists in the database$")
    public void that_a_user_entry_with_the_username_someone_institution_exists_in_the_database(String username)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        UserDto existingUser = UserDto.newBuilder().withUsername(username).build();
        getDatabaseService().addUser(existingUser);
    }

    @When("the authorized client sends the request to read the user")
    public void theAuthorizedClientSendsTheRequestToReadTheUser() throws IOException {
        GetUserHandler getUserHandler = new GetUserHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(getUserHandler);
    }
}
