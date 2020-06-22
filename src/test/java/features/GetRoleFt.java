package features;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import no.unit.nva.exceptions.InvalidInputRoleException;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.model.RoleDto;
import nva.commons.utils.JsonUtils;

public class GetRoleFt extends ScenarioTest {

    private final ScenarioContext scenarioContext;
    private RoleDto expectedRoleDto;
    private RoleDto actualRoleDto;

    /**
     * Default Constructor.
     *
     * @param scenarioContext the injected scenario context.
     */
    public GetRoleFt(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("that there is a role with role-name {string}")
    public void that_there_is_a_role_with_rolename(String roleName)
        throws InvalidInputRoleException, InvalidRoleInternalException {

        RoleDto newRole = RoleDto.newBuilder().withName(roleName).build();
        expectedRoleDto = newRole;
        scenarioContext.getDatabaseService().addRole(newRole);
    }

    @When("the authorized client sends a GET request to the path {string}")
    public void the_authorized_client_sends_a_GET_request_to_the_path(String path) {
        //TODO: implement handler
    }

    @Then("a role description is returned")
    public void a_role_description_is_returned() {
        // Write code here that turns the phrase above into concrete actions
        //TODO: read the response
    }

    @Then("the role description contains the following fields and respective values:")
    public void the_role_description_contains_the_following_fields_and_respective_values(DataTable dataTable) {
        Map<String, String> fieldValuePairs = dataTable.rows(IGNORE_HEADER_ROW).asMap(String.class, String.class);
        JsonNode roleJson = JsonUtils.objectMapper.convertValue(actualRoleDto, JsonNode.class);
        //TODO: perform comparison between expected and actual values

    }
}
