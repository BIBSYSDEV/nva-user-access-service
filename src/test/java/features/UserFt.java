package features;

import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.handlers.GetUserHandler;
import no.unit.nva.handlers.UpdateUserHandler;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.utils.EntityUtils;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.attempt.Try;
import org.apache.http.HttpStatus;
import org.zalando.problem.Problem;

public class UserFt extends ScenarioTest {

    public static final int SIZE_IS_NOT_IMPORTANT = 0;

    private UserDto existingUser;

    public UserFt(ScenarioContext scenarioContext) {
        super(scenarioContext);
    }

    @Given("that a user entry with the username {string} exists in the database")
    public void that_a_user_entry_with_the_username_someone_institution_exists_in_the_database(String username)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        existingUser = UserDto.newBuilder().withUsername(username).build();
        getDatabaseService().addUser(existingUser);
    }

    @Given("the user entry contains a list of roles with the following role-names")
    public void the_user_entry_contains_a_list_of_roles_with_the_following_role_names(DataTable dataTable)
        throws InvalidEntryInternalException, NotFoundException, InvalidInputException {

        List<RoleDto> expectedRoles = createRolesFromDatatable(dataTable);
        UserDto userUpdate = updateExistingUserDirectlyInDatabase(expectedRoles);
        UserDto updatedUser = getUserDirectlyFromDatabase(userUpdate);

        assertThat(updatedUser.getRoles(), containsInAnyOrder(expectedRoles.toArray()));

        assertThatExpectedRolesAndActualRolesAreEqual(expectedRoles, updatedUser);
    }

    @Given("the request contains a malformed JSON body")
    public void the_request_contains_a_malformed_Json_body()
        throws InvocationTargetException, InvalidEntryInternalException, IllegalAccessException, NoSuchMethodException,
               JsonProcessingException {

        Map<String, Object> invalidRequestBody = createInvalidRequestBody();
        HandlerRequestBuilder<Map<String, Object>> updatedRequestBuilder = scenarioContext
            .getRequestBuilder().withBody(invalidRequestBody);
        scenarioContext.setRequestBuilder(updatedRequestBuilder);
    }

    @When("the authorized client sends the request to update the user")
    public void the_authorized_client_sends_the_request_to_update_the_user() throws IOException {
        UpdateUserHandler updateUserHandler = new UpdateUserHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(updateUserHandler);
    }

    @When("the authorized client sends the request to read the user")
    public void theAuthorizedClientSendsTheRequestToReadTheUser() throws IOException {
        GetUserHandler getUserHandler = new GetUserHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(getUserHandler);
    }

    @Then("the user entry is updated asynchronously")
    public void the_user_entry_is_updated_asynchronously()
        throws JsonProcessingException, InvalidEntryInternalException, NotFoundException {
        UserDto expectedUser = readRequestBody(scenarioContext.getRequestBuilder(), UserDto.class);
        UserDto actualUser = getUserDirectlyFromDatabase(expectedUser);
        assertThat(actualUser, is(equalTo(expectedUser)));
    }

    @Then("a Location header with the updated user URI is included in the response")
    public void a_Location_header_with_the_updated_user_URI_is_included_in_the_response() throws IOException {
        GatewayResponse<UserDto> response = scenarioContext.getApiGatewayResponse(UserDto.class);
        String locationHeader = response.getHeaders().get(UpdateUserHandler.LOCATION_HEADER);
        assertThat(locationHeader, is(not(emptyOrNullString())));
        assertThat(locationHeader, containsString(existingUser.getUsername()));
    }

    @Then("a BadRequest message is returned containing information about the invalid request")
    public void a_BadRequest_message_is_returned() throws IOException {
        GatewayResponse<Problem> response = scenarioContext.getApiGatewayResponse(Problem.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
        Problem problem = response.getBodyObject(Problem.class);
        assertThat(problem.getDetail(), containsString(UserDto.INVALID_USER_ERROR_MESSAGE));
    }

    private Map<String, Object> createInvalidRequestBody()
        throws InvalidEntryInternalException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        UserDto invalidUser = EntityUtils.createUserWithoutUsername();
        return transformUserDtoToMap(invalidUser);
    }

    private Map<String, Object> transformUserDtoToMap(UserDto invalidUser) {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        return JsonUtils.objectMapper.convertValue(invalidUser, typeRef);
    }

    private void assertThatExpectedRolesAndActualRolesAreEqual(List<RoleDto> expectedRoles, UserDto updatedUser) {
        RoleDto[] actualRolesArray = updatedUser.getRoles().toArray(new RoleDto[SIZE_IS_NOT_IMPORTANT]);
        RoleDto[] expectedRolesArray = expectedRoles.toArray(new RoleDto[SIZE_IS_NOT_IMPORTANT]);
        assertThat(updatedUser.getRoles(), containsInAnyOrder(expectedRolesArray));
        assertThat(expectedRoles, containsInAnyOrder(actualRolesArray));
    }

    private UserDto updateExistingUserDirectlyInDatabase(List<RoleDto> expectedRoles)
        throws InvalidEntryInternalException, NotFoundException, InvalidInputException {

        UserDto currentUser = getUserDirectlyFromDatabase(existingUser);
        UserDto userUpdate = currentUser.copy().withRoles(expectedRoles).build();
        getDatabaseService().updateUser(userUpdate);

        return userUpdate;
    }

    private UserDto getUserDirectlyFromDatabase(UserDto existingUser)
        throws InvalidEntryInternalException, NotFoundException {
        return getDatabaseService().getUser(existingUser);
    }

    private List<RoleDto> createRolesFromDatatable(DataTable dataTable) {
        return ignoreHeadersRow(dataTable)
            .asList()
            .stream()
            .map(attempt(roleName -> RoleDto.newBuilder().withName(roleName).build()))
            .flatMap(Try::stream)
            .collect(Collectors.toList());
    }
}
