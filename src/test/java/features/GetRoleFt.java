package features;

import static nva.commons.utils.JsonUtils.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.collection.IsIn.in;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import no.unit.nva.exceptions.InvalidInputRoleException;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.handlers.GetRoleHandler;
import no.unit.nva.model.RoleDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;

public class GetRoleFt extends ScenarioTest {

    private final ScenarioContext scenarioContext;
    private final Context context = mock(Context.class);
    private RoleDto expectedRoleDto;
    private RoleDto actualRoleDto;
    private HandlerRequestBuilder<Void> requestBuilder;

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

    @When("the authorized client sends a {string} request with the following path parameters:")
    public void the_authorized_client_sends_a_GET_request_to_the_path(String method, DataTable pathTable) {
        Map<String, String> pathParameters = pathTable.rows(IGNORE_HEADER_ROW).asMap(String.class, String.class);
        this.requestBuilder = new HandlerRequestBuilder<Void>(objectMapper).withHttpMethod(method)
            .withPathParameters(pathParameters);
    }

    @Then("a role description is returned")
    public void a_role_description_is_returned() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = this.requestBuilder.build();
        GetRoleHandler getRoleHandler = new GetRoleHandler(mockEnvironment(), scenarioContext.getDatabaseService());
        getRoleHandler.handleRequest(inputStream, outputStream, context);
        GatewayResponse<RoleDto> response = GatewayResponse.fromOutputStream(outputStream);
        actualRoleDto = response.getBodyObject(RoleDto.class);
    }

    @Then("the role description contains the following fields and respective values:")
    public void the_role_description_contains_the_following_fields_and_respective_values(DataTable dataTable)
        throws IOException {
        Map<String, String> expectedFieldValuePairs = dataTable.rows(IGNORE_HEADER_ROW)
            .asMap(String.class, String.class);
        Map<String, String> actualFieldValuePairs = propertyValues(actualRoleDto);
        compareExpectedAndActualValues(expectedFieldValuePairs, actualFieldValuePairs);
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
