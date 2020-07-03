package features;

import static features.ScenarioTest.IGNORE_HEADER_ROW;
import static features.ScenarioTest.createRequestBuilderTypeRef;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.handlers.AddRoleHandler;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.utils.JsonUtils;

public class ScenarioCommons extends DatabaseAccessor {

    private final ScenarioContext scenarioContext;

    public ScenarioCommons(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("a database for users and roles")
    public void a_database_for_users_and_roles() {
        DynamoDBMapper mapper = new DynamoDBMapper(initializeTestDatabase());
        this.scenarioContext.setDatabaseService(new DatabaseServiceImpl(mapper));
    }

    @Given("the authorized client forms a {string} request")
    public void the_authorized_client_forms_a_request(String httpMethod) {
        this.scenarioContext.setRequestBuilder(createRequestBuilder());
        scenarioContext.getRequestBuilder().withHttpMethod(httpMethod.toUpperCase());
    }

    private HandlerRequestBuilder<Map<String, Object>> createRequestBuilder() {
        return new HandlerRequestBuilder<>(JsonUtils.objectMapper);
    }

    @Given("the request contains a JSON body with following key-value pairs")
    public void the_request_contains_a_Json_body_with_following_key_value_pairs(DataTable dataTable)
        throws JsonProcessingException {
        DataTable inputData = dataTable.rows(IGNORE_HEADER_ROW);
        addFieldsToRequestBody(inputData);
    }

    @When("the authorized client sends the request")
    public void the_authorized_client_sends_the_request() throws IOException {
        InputStream request = buildRequestInputStream();
        ByteArrayOutputStream outputStream = invokeAddRoleHandler(request);
        scenarioContext.setRequestResponse(outputStream.toString());
    }

    private ByteArrayOutputStream invokeAddRoleHandler(InputStream request) throws IOException {
        AddRoleHandler addRoleHandler = new AddRoleHandler(mockEnvironment(), scenarioContext.getDatabaseService());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Context context = mock(Context.class);
        addRoleHandler.handleRequest(request, outputStream, context);
        return outputStream;
    }

    private InputStream buildRequestInputStream() throws JsonProcessingException {
        return scenarioContext.getRequestBuilder().build();
    }

    private void addFieldsToRequestBody(DataTable inputData) throws JsonProcessingException {
        Map<String, Object> bodyFields = inputData.asMap(String.class, Object.class);
        Map<String, Object> body = fetchOrCreateRequestBody();
        body.putAll(bodyFields);
        scenarioContext.setRequestBuilder(scenarioContext.getRequestBuilder().withBody(body));
    }

    private Map<String, Object> fetchOrCreateRequestBody() throws JsonProcessingException {
        return Optional.ofNullable(scenarioContext.getRequestBuilder()
            .getBody(createRequestBuilderTypeRef()))
            .orElse(new ConcurrentHashMap<>());
    }

    /**
     * After each scenario close the local database.
     */
    @After
    @Override
    public void closeDB() {
        super.closeDB();
    }
}
