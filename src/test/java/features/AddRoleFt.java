package features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.handlers.AddRoleHandler;
import no.unit.nva.model.RoleDto;
import nva.commons.utils.JsonUtils;

public class AddRoleFt extends ScenarioTest {

    public AddRoleFt(ScenarioContext scenarioContext) {
        super(scenarioContext);
    }

    @Given("an authorized client")
    public void an_authorized_client() {
        // do nothing
    }

    @When("the authorized client sends the request to add a new Role")
    public void the_authorized_client_sends_the_request_to_add_a_new_Role() throws IOException {
        AddRoleHandler addRoleHandler = new AddRoleHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(addRoleHandler);
    }

    @Then("a new role is stored in the database")
    public void a_new_role_is_stored_in_the_database() throws InvalidRoleInternalException, IOException {

        RoleDto responseObject = getResponseBody();
        Optional<RoleDto> objectReadDirectlyFromDatabase = readRoleDirectlyFromDatabase(responseObject);
        assertTrue(objectReadDirectlyFromDatabase.isPresent());
        assertThat(objectReadDirectlyFromDatabase.get(), is(equalTo(responseObject)));
        assertThat(objectReadDirectlyFromDatabase.get(), is(not(sameInstance(responseObject))));
    }

    @Then("the description of the role is returned to the authorized client")
    public void the_description_of_the_role_is_returned_to_the_authorized_client() throws IOException {
        RoleDto responseObject = getResponseBody();
        Map<String, Object> requestObjectMap = fetchOriginalRequestParametersAsMap();
        RoleDto requestObject = convertRequestBodyToRoleDto(requestObjectMap);
        assertThat(requestObject, is(equalTo(responseObject)));
    }

    private Optional<RoleDto> readRoleDirectlyFromDatabase(RoleDto responseObject) throws InvalidRoleInternalException {
        return getDatabaseService().getRole(responseObject);
    }

    private RoleDto convertRequestBodyToRoleDto(Map<String, Object> requestObjectMap) {
        return JsonUtils.objectMapper.convertValue(requestObjectMap, RoleDto.class);
    }

    private RoleDto getResponseBody() throws IOException {
        return getResponseBody(RoleDto.class);
    }

    private Map<String, Object> fetchOriginalRequestParametersAsMap()
        throws JsonProcessingException {
        return getRequestBuilder().getBody(createRequestBuilderTypeRef());
    }
}
