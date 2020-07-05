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
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.handlers.AddRoleHandler;
import no.unit.nva.model.RoleDto;
import nva.commons.utils.JsonUtils;

public class AddRoleFt extends ScenarioTest {

    private final ScenarioContext scenarioContext;

    public AddRoleFt(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("an authorized client")
    public void an_authorized_client() {
        // do nothing
    }

    @Given("that the authorized client intends to add a new Role")
    public void that_an_authorized_client_intends_to_add_new_Role_withe_role_name() {
        Supplier<AddRoleHandler> addRoleHandlerSupplier =
            () -> new AddRoleHandler(mockEnvironment(), scenarioContext.getDatabaseService());
        scenarioContext.setHandlerSupplier(addRoleHandlerSupplier);
    }

    @Then("a new role is stored in the database")
    public void a_new_role_is_stored_in_the_database() throws InvalidRoleInternalException, IOException {

        RoleDto responseObject = fetchResponseBody();
        Optional<RoleDto> objectReadDirectlyFromDatabase = readRoleDirectlyFromDatabase(responseObject);
        assertTrue(objectReadDirectlyFromDatabase.isPresent());
        assertThat(objectReadDirectlyFromDatabase.get(), is(equalTo(responseObject)));
        assertThat(objectReadDirectlyFromDatabase.get(), is(not(sameInstance(responseObject))));
    }

    @Then("the description of the role is returned to the authorized client")
    public void the_description_of_the_role_is_returned_to_the_authorized_client() throws IOException {
        RoleDto responseObject = fetchResponseBody();
        Map<String, Object> requestObjectMap = fetchOriginalRequestParametersAsMap();
        RoleDto requestObject = convertRequestBodyToRoleDto(requestObjectMap);
        assertThat(requestObject, is(equalTo(responseObject)));
    }

    private Optional<RoleDto> readRoleDirectlyFromDatabase(RoleDto responseObject) throws InvalidRoleInternalException {
        return scenarioContext.getDatabaseService().getRole(responseObject);
    }

    private RoleDto convertRequestBodyToRoleDto(Map<String, Object> requestObjectMap) {
        return JsonUtils.objectMapper.convertValue(requestObjectMap, RoleDto.class);
    }

    private RoleDto fetchResponseBody() throws IOException {
        return scenarioContext.getResponseBody(RoleDto.class);
    }

    private Map<String, Object> fetchOriginalRequestParametersAsMap()
        throws JsonProcessingException {
        return scenarioContext.getRequestBuilder()
            .getBody(createRequestBuilderTypeRef());
    }
}
