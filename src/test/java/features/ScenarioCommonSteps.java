package features;

import static features.ScenarioTest.IGNORE_HEADER_ROW;
import static features.ScenarioTest.createRequestBuilderTypeRef;
import static features.ScenarioTest.ignoreHeadersRow;
import static nva.commons.utils.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.attempt.Try;
import org.apache.http.HttpStatus;
import org.zalando.problem.Problem;

public class ScenarioCommonSteps extends DatabaseAccessor {

    private final ScenarioContext scenarioContext;

    public ScenarioCommonSteps(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("a Database for users and roles")
    public void a_database_for_users_and_roles() {
        this.scenarioContext.setDatabaseService(createDatabaseServiceUsingLocalStorage());
    }

    @Given("an AuthorizedClient that is authorized through Feide")
    public void an_AuthorizedClient_that_is_authorized_through_Feide() {
        //DO NOTHING
    }

    @Then("a NotFound message is returned")
    public void a_NotFound_message_is_returned() throws IOException {
        GatewayResponse<Problem> response = scenarioContext.getApiGatewayResponse(Problem.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_NOT_FOUND)));
    }

    /**
     * After each scenario close the local database.
     */
    @After
    @Override
    public void closeDB() {
        super.closeDB();
    }

    private HandlerRequestBuilder<Map<String, Object>> createRequestBuilder() {
        return new HandlerRequestBuilder<>(JsonUtils.objectMapper);
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
}
