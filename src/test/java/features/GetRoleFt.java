package features;

import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsIn.in;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.handlers.GetRoleHandler;
import no.unit.nva.model.RoleDto;
import no.unit.nva.testutils.HandlerRequestBuilder;

public class GetRoleFt extends ScenarioTest {

    private final Context context = mock(Context.class);
    private RoleDto actualRoleDto;

    /**
     * Default Constructor.
     *
     * @param scenarioContext the injected scenario context.
     */
    public GetRoleFt(ScenarioContext scenarioContext) {
        super(scenarioContext);
    }

    @Given("that there is a role with role-name {string}")
    public void that_there_is_a_role_with_rolename(String roleName)
        throws InvalidEntryInternalException, ConflictException, InvalidInputException {

        RoleDto newRole = RoleDto.newBuilder().withName(roleName).build();
        getDatabaseService().addRole(newRole);
    }

    @Given("that there is no role with role-name {string}")
    public void that_there_is_no_role_with_role_name(String roleName) throws InvalidEntryInternalException {
        RoleDto queryObject = RoleDto.newBuilder().withName(roleName).build();
        Optional<RoleDto> queryResult = getDatabaseService().getRoleAsOptional(queryObject);
        assertThat(queryResult.isEmpty(), is(equalTo(true)));
    }

    @Given("the request has the following path parameters:")
    public void the_request_has_the_following_path_parameters(io.cucumber.datatable.DataTable pathTable) {
        Map<String, String> pathParameters = pathTable.rows(IGNORE_HEADER_ROW).asMap(String.class, String.class);
        setRequestBuilder(new HandlerRequestBuilder<Map<String, Object>>(objectMapper)
            .withPathParameters(pathParameters));
    }

    @Then("a role description is returned")
    public void a_role_description_is_returned() throws IOException {
        actualRoleDto = getResponseBody();
        assertThat(actualRoleDto, is(notNullValue()));
    }

    @Then("the role description contains the following fields and respective values:")
    public void the_role_description_contains_the_following_fields_and_respective_values(DataTable dataTable)
        throws IOException {
        Map<String, String> expectedFieldValuePairs = dataTable.rows(IGNORE_HEADER_ROW)
            .asMap(String.class, String.class);
        Map<String, String> actualFieldValuePairs = propertyValues(actualRoleDto);
        compareExpectedAndActualValues(expectedFieldValuePairs, actualFieldValuePairs);
    }

    @When("the authorized client sends the request to read the role")
    public void the_authorized_client_sends_the_request_to_read_the_role() throws IOException {
        GetRoleHandler getRoleHandler = new GetRoleHandler(mockEnvironment(), getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(getRoleHandler);
    }

    private RoleDto getResponseBody() throws IOException {
        return getResponseBody(RoleDto.class);
    }

    private void compareExpectedAndActualValues(Map<String, String> fieldValuePairs, Map<String, String> propValues) {
        Set<String> expectedProperties = fieldValuePairs.keySet();
        Set<String> actualProperties = propValues.keySet();
        assertThat(expectedProperties, everyItem(is(in(actualProperties))));

        assertThat(actualProperties, contains(expectedProperties.toArray()));
    }

    private <T> Map<String, String> propertyValues(T object) throws IOException {

        String json = objectMapper.writeValueAsString(object);
        TypeReference<Map<String, String>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(json, typeReference);
    }
}
