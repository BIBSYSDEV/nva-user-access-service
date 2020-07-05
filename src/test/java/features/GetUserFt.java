package features;

import io.cucumber.java.en.Given;
import java.util.function.Supplier;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidUserInternalException;
import no.unit.nva.handlers.GetUserHandler;
import no.unit.nva.model.UserDto;

public class GetUserFt extends ScenarioTest {

    private final ScenarioContext scenarioContext;

    public GetUserFt(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("that the authorized client intends to read a User")
    public void that_the_authorized_client_intends_to_read_a_User() {
        Supplier<GetUserHandler> handlerSupplier =
            () -> new GetUserHandler(mockEnvironment(), scenarioContext.getDatabaseService());
        scenarioContext.setHandlerSupplier(handlerSupplier);
    }

    @Given("^that a user entry with the username (.*) exists in the database$")
    public void that_a_user_entry_with_the_username_someone_institution_exists_in_the_database(String username)
        throws InvalidUserInternalException, ConflictException {
        UserDto existingUser = UserDto.newBuilder().withUsername(username).build();
        scenarioContext.getDatabaseService().addUser(existingUser);
    }
}
