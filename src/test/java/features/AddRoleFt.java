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
import java.util.Map;
import java.util.Optional;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.model.RoleDto;
import nva.commons.handlers.GatewayResponse;
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


    @Then("a new role is stored in the database")
    public void a_new_role_is_stored_in_the_database() throws InvalidRoleInternalException, JsonProcessingException {
        GatewayResponse<RoleDto> response = GatewayResponse.fromString(scenarioContext.getRequestResponse());
        RoleDto resultObject = response.getBodyObject(RoleDto.class);
        Optional<RoleDto> savedRole = scenarioContext.getDatabaseService().getRole(resultObject);
        assertTrue(savedRole.isPresent());
        assertThat(savedRole.get(), is(equalTo(resultObject)));
        assertThat(savedRole.get(), is(not(sameInstance(resultObject))));
    }

    @Then("the description of the role is returned to the authorized client")
    public void the_description_of_the_role_is_returned_to_the_authorized_client() throws JsonProcessingException {
        GatewayResponse<RoleDto> response = GatewayResponse.fromString(scenarioContext.getRequestResponse());
        RoleDto responseObject = response.getBodyObject(RoleDto.class);
        Map<String, Object> requestObjectMap = scenarioContext.getRequestBuilder().getBody(createRequestBuilderTypeRef());
        RoleDto requestObject = JsonUtils.objectMapper.convertValue(requestObjectMap, RoleDto.class);
        assertThat(requestObject, is(equalTo(responseObject)));
    }




}
