package features;

import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import no.unit.nva.exceptions.InvalidUserInternalException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.attempt.Try;

public class AddUserFt extends ScenarioTest{

    public static final String HTTP_POST_METHOD = "post";
    private final ScenarioContext scenarioContext;
    private List<RoleDto> expectedRoles;
    private UserDto resultUser;

    public AddUserFt(ScenarioContext scenarioContext){
        this.scenarioContext = scenarioContext;
    }



    @Given("that a user entry with the username {string} does not exist in the database")
    public void that_a_user_entry_with_the_username_does_not_exist_in_the_database(String username)
        throws InvalidUserInternalException {
        UserDto queryObject = UserDto.newBuilder().withUsername(username).build();
        Optional<UserDto> queryResult = scenarioContext.getDatabaseService().getUser(queryObject);
        assertTrue(queryResult.isEmpty());
    }

    @When("the authorized client sends a POST request")
    public void the_authorized_client_sends_a_POST_request() {
        scenarioContext.setRequestBuilder(new HandlerRequestBuilder<Map<String,Object>>(JsonUtils.objectMapper)
            .withHttpMethod(HTTP_POST_METHOD));
    }

    @When("the request body also contains a list of roles with the following role-names")
    public void the_request_body_also_contains_a_list_of_roles_with_the_following_role_names(DataTable dataTable)
        throws JsonProcessingException, InvalidUserInternalException {

        expectedRoles = extractRolesFromScenarioInput(dataTable);

        UserDto currentRequestBody = readRequestBody(scenarioContext.getRequestBuilder(), UserDto.class);
        UserDto updatedRequestBody = currentRequestBody.copy().withRoles(expectedRoles).build();

        HandlerRequestBuilder<Map<String, Object>> requestBuilder = scenarioContext.getRequestBuilder()
            .withBody(prepareRequestBody(updatedRequestBody));
        scenarioContext.setRequestBuilder(requestBuilder);

    }

    private List<RoleDto> extractRolesFromScenarioInput(DataTable dataTable) {
        List<RoleDto> roles = createRoleListFromRoleNames(dataTable);
        assertThat(roles.size(),is(equalTo(ignoreHeadersRow(dataTable).asList().size())));
        return roles;
    }

    private List<RoleDto> createRoleListFromRoleNames(DataTable dataTable) {
        return ignoreHeadersRow(dataTable).asList().stream()
            .map(attempt(roleName -> RoleDto.newBuilder().withName(roleName).build()))
            .flatMap(Try::stream)
            .collect(Collectors.toList());
    }

    @Then("the handler returns a user object")
    public void the_handler_returns_a_user_object() throws IOException {
        resultUser = JsonUtils.objectMapper.readValue(scenarioContext.getRequestResponse(), UserDto.class);
    }

    @Then("the user object contains all the aforementioned information")
    public void the_user_object_contains_all_the_aforementioned_information() throws JsonProcessingException {
        UserDto expectedUser = readRequestBody(scenarioContext.getRequestBuilder(), UserDto.class);
        assertThat(resultUser,is(equalTo(expectedUser)));
    }





}
