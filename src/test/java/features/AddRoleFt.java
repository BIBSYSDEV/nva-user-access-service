package features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
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
import no.unit.nva.database.DatabaseService;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.DatabaseTest;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.handlers.AddRoleHandler;
import no.unit.nva.model.RoleDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.JsonUtils;

public class AddRoleFt extends DatabaseTest {

    public static final int IGNORE_HEADER_ROW = 1;
    private DynamoDBMapper mapper;
    private DatabaseService service;
    private String requestResponse;
    private Map<String, Object> requestBody;

    @Given("a database for users and roles")
    public void a_database_for_users_and_roles() {
        mapper = new DynamoDBMapper(initializeTestDatabase());
        service = new DatabaseServiceImpl(mapper);
    }

    @Given("an authorized client")
    public void an_authorized_client() {
        // do nothing
    }

    @When("then authorized client sends a POST request")
    public void then_authorized_client_sends_a_Post_request() {
        // cannot imitate generating a post request with currently no parameters when running internally.
    }

    @When("the request contains a JSON body with following key-value pairs")
    public void the_request_contains_a_Json_body_with_following_key_value_pairs(DataTable dataTable)
        throws IOException {
        DataTable inputData = dataTable.rows(IGNORE_HEADER_ROW);
        requestBody = inputData.asMap(String.class, Object.class);
        String body = JsonUtils.objectMapper.writeValueAsString(requestBody);
        InputStream request = new HandlerRequestBuilder<String>(JsonUtils.objectMapper)
            .withBody(body).build();
        AddRoleHandler addRoleHandler = new AddRoleHandler(mockEnvironment(), service);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Context context = mock(Context.class);
        addRoleHandler.handleRequest(request, outputStream, context);
        requestResponse = outputStream.toString();
    }

    @Then("a new role is stored in the database")
    public void a_new_role_is_stored_in_the_database() throws InvalidRoleException, JsonProcessingException {
        GatewayResponse<RoleDto> response = GatewayResponse.fromString(requestResponse);
        RoleDto resultObject = response.getBodyObject(RoleDto.class);
        DatabaseService serviceImpl = new DatabaseServiceImpl(mapper);
        Optional<RoleDto> savedRole = serviceImpl.getRole(resultObject);
        assertTrue(savedRole.isPresent());
        assertThat(savedRole.get(), is(equalTo(resultObject)));
        assertThat(savedRole.get(), is(not(sameInstance(resultObject))));
    }

    @Then("the description of the role is returned to the authorized client")
    public void the_description_of_the_role_is_returned_to_the_authorized_client() throws JsonProcessingException {
        GatewayResponse<RoleDto> response = GatewayResponse.fromString(requestResponse);
        RoleDto responseObject = response.getBodyObject(RoleDto.class);
        RoleDto requestObject = JsonUtils.objectMapper.convertValue(requestBody, RoleDto.class);
        assertThat(requestObject, is(equalTo(responseObject)));
    }
}
