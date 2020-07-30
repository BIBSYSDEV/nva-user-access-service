package features;

import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
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
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.handlers.AddUserHandler;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.utils.attempt.Try;

public class AddUserFt extends ScenarioTest {

    public static final String HTTP_POST_METHOD = "post";
    private UserDto resultUser;

    public AddUserFt(ScenarioContext scenarioContext) {
        super(scenarioContext);
    }

    @Given("that a User with username {string} does not exist in the database")
    public void that_a_user_entry_with_the_username_does_not_exist_in_the_database(String username)
        throws InvalidEntryInternalException {
        UserDto queryObject = UserDto.newBuilder().withUsername(username).build();
        Optional<UserDto> queryResult = fetchUserDirectlyFromDatabase(queryObject);
        assertTrue(queryResult.isEmpty());
    }

    @When("the authorized client sends a POST request")
    public void the_authorized_client_sends_a_Post_request() {
        HandlerRequestBuilder<Map<String, Object>> requestBuilder =
            new HandlerRequestBuilder<Map<String, Object>>(objectMapper)
                .withHttpMethod(HTTP_POST_METHOD);
        setRequestBuilder(requestBuilder);
    }

    @When("the request body also contains a list of roles with the following role-names")
    public void the_request_body_also_contains_a_list_of_roles_with_the_following_role_names(DataTable dataTable)
        throws JsonProcessingException, InvalidEntryInternalException {

        UserDto updatedRequestObject = addDesiredRolesToRequestObject(dataTable);

        HandlerRequestBuilder<Map<String, Object>> requestBuilder =
            convertRequestObjectToRequestBody(updatedRequestObject);

        setRequestBuilder(requestBuilder);
    }

    @Then("a user description is returned")
    public void a_user_description_is_returned() throws IOException {
        resultUser = getResponseBody();
        assertThat(resultUser, is(notNullValue()));
    }

    @Then("the user object contains all the aforementioned information")
    public void the_user_object_contains_all_the_aforementioned_information() throws JsonProcessingException {
        UserDto expectedUser = readRequestBody(getRequestBuilder(), UserDto.class);
        assertThat(resultUser, is(equalTo(expectedUser)));
    }

    @When("the authorized client sends the request to add a new User")
    public void the_authorized_client_sends_the_request_to_add_a_new_user() throws IOException {
        AddUserHandler handler = new AddUserHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(handler);
    }

    private UserDto getResponseBody() throws IOException {
        return getResponseBody(UserDto.class);
    }

    private HandlerRequestBuilder<Map<String, Object>> convertRequestObjectToRequestBody(UserDto updatedRequestBody)
        throws JsonProcessingException {
        return getRequestBuilder()
            .withBody(prepareRequestBody(updatedRequestBody));
    }

    private UserDto addDesiredRolesToRequestObject(DataTable dataTable)
        throws JsonProcessingException, InvalidEntryInternalException {
        List<RoleDto> desiredRoles = extractRolesFromScenarioInput(dataTable);
        UserDto currentRequestObject = readRequestBody(getRequestBuilder(), UserDto.class);
        return currentRequestObject.copy().withRoles(desiredRoles).build();
    }

    private Optional<UserDto> fetchUserDirectlyFromDatabase(UserDto queryObject) throws InvalidEntryInternalException {
        return getDatabaseService().getUserAsOptional(queryObject);
    }

    private List<RoleDto> extractRolesFromScenarioInput(DataTable dataTable) {
        List<RoleDto> roles = createRoleListFromRoleNames(dataTable);
        assertThat(roles.size(), is(equalTo(ignoreHeadersRow(dataTable).asList().size())));
        return roles;
    }

    private List<RoleDto> createRoleListFromRoleNames(DataTable dataTable) {
        return ignoreHeadersRow(dataTable).asList().stream()
            .map(attempt(roleName -> RoleDto.newBuilder().withName(roleName).build()))
            .flatMap(Try::stream)
            .collect(Collectors.toList());
    }
}
