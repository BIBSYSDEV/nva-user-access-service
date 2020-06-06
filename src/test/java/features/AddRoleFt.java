package features;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.DatabaseTest;

public class AddRoleFt extends DatabaseTest {

    private DynamoDBMapper mapper;
    private DatabaseService service;

    @Given("a database for users and roles")
    public void a_database_for_users_and_roles() {
        mapper = new DynamoDBMapper(initializeTestDatabase());
        service = new DatabaseServiceImpl(mapper);
    }

    @Given("an authorized client")
    public void an_authorized_client() {
        // do nothing
    }

    @When("then authorized client sends a createRole request with following parameters")
    public void then_authorized_client_sends_a_createRole_request_with_following_parameters(io.cucumber.datatable.DataTable dataTable) {

    }

    @Then("a new role is stored in the database")
    public void a_new_role_is_stored_in_the_database() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("the description of the role is returned to the authorized client")
    public void the_description_of_the_role_is_returned_to_the_authorized_client() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }


}
