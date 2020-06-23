package features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.database.exceptions.InvalidRoleInternalException;
import no.unit.nva.handlers.AddRoleHandler;
import no.unit.nva.model.RoleDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.JsonUtils;

public class AddRoleFt extends ScenarioTest {

    public static final String HTTP_METHOD = "httpMethod";
    private final ScenarioContext scenarioContext;
    private String requestResponse;

    public AddRoleFt(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("an authorized client")
    public void an_authorized_client() {
        // do nothing
    }

    @When("the authorized client sends a {string} request")
    public void the_authorized_client_sends_a_request(String httpMethod) {
        this.scenarioContext.setRequestBody(new ConcurrentHashMap<>());
        scenarioContext.getRequestBody().put(HTTP_METHOD, httpMethod.toUpperCase());
    }

    @When("the request contains a JSON body with following key-value pairs")
    public void the_request_contains_a_Json_body_with_following_key_value_pairs(DataTable dataTable)
        throws IOException {
        DataTable inputData = dataTable.rows(IGNORE_HEADER_ROW);
        addFieldsToRequestBody(inputData);

        InputStream request = buildRequestInputStream();

        ByteArrayOutputStream outputStream = invokeAddRoleHandler(request);

        requestResponse = outputStream.toString();
    }

    @Then("a new role is stored in the database")
    public void a_new_role_is_stored_in_the_database() throws InvalidRoleInternalException, JsonProcessingException {
        GatewayResponse<RoleDto> response = GatewayResponse.fromString(requestResponse);
        RoleDto resultObject = response.getBodyObject(RoleDto.class);
        Optional<RoleDto> savedRole = scenarioContext.getDatabaseService().getRole(resultObject);
        assertTrue(savedRole.isPresent());
        assertThat(savedRole.get(), is(equalTo(resultObject)));
        assertThat(savedRole.get(), is(not(sameInstance(resultObject))));
    }

    @Then("the description of the role is returned to the authorized client")
    public void the_description_of_the_role_is_returned_to_the_authorized_client() throws JsonProcessingException {
        GatewayResponse<RoleDto> response = GatewayResponse.fromString(requestResponse);
        RoleDto responseObject = response.getBodyObject(RoleDto.class);
        RoleDto requestObject = JsonUtils.objectMapper.convertValue(scenarioContext.getRequestBody(), RoleDto.class);
        assertThat(requestObject, is(equalTo(responseObject)));
    }

    private ByteArrayOutputStream invokeAddRoleHandler(InputStream request) throws IOException {
        AddRoleHandler addRoleHandler = new AddRoleHandler(mockEnvironment(), scenarioContext.getDatabaseService());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Context context = mock(Context.class);
        addRoleHandler.handleRequest(request, outputStream, context);
        return outputStream;
    }

    private InputStream buildRequestInputStream() throws JsonProcessingException {
        String body = JsonUtils.objectMapper.writeValueAsString(scenarioContext.getRequestBody());
        return new HandlerRequestBuilder<String>(JsonUtils.objectMapper)
            .withBody(body).build();
    }

    private void addFieldsToRequestBody(DataTable inputData) {
        Map<String, Object> bodyFields = inputData.asMap(String.class, Object.class);
        scenarioContext.getRequestBody().putAll(bodyFields);
    }
}
