package features;

import static java.util.Objects.nonNull;
import static no.unit.nva.handlers.InternalServiceMock.CORRECT_API_KEY;
import static no.unit.nva.handlers.InternalServiceMock.WRONG_API_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import no.unit.nva.handlers.ApiKeyValidation;
import no.unit.nva.handlers.GetUserHandlerForInternalService;
import no.unit.nva.handlers.InternalServiceMock;
import no.unit.nva.model.UserDto;
import nva.commons.handlers.ApiGatewayHandler;

public class InternalServiceAccessFt extends ScenarioTest {

    public InternalServiceAccessFt(ScenarioContext scenarioContext) {
        super(scenarioContext);
    }

    @Given("an Internal Service with an API key")
    public void an_Internal_Service_with_an_API_key() {
        //DO NOTHING
    }

    @When("^the InternalService requests to get the (\\w*) using a valid API key$")
    public void the_InternalService_requests_to_get_the_user_using_a_valid_API_key(String userAlias)
        throws IOException {
        createValidGetRequest(userAlias);
        runGetRequest();
    }

    @When("^the InternalService requests to get the (\\w*) using an invalid API key$")
    public void the_InternalService_requests_to_get_the_user_using_a_invalid_API_key(String userAlias)
        throws IOException {
        createGetRequestWithInvalidApiKey(userAlias);
        runGetRequest();
    }

    @When("^the InternalService requests to get the (\\w*) without an API key$")
    public void the_InternalService_requests_to_get_the_user_without_an_api_key(String userAlias)
        throws IOException {
        createGetRequestWithoutAnApiKey(userAlias);
        runGetRequest();
    }

    @When("^the InternalService requests to add the (\\w*) using a valid API key$")
    public void the_InternalService_requests_to_add_a_user_using_a_valid_API_key(String userAlias)
        throws IOException {
        crateValidAddRequest(userAlias);
        runAddRequest();
    }

    @When("^the InternalService requests to add the (\\w*) using an invalid API key$")
    public void the_InternalService_requests_to_add_a_user_using_a_invalid_API_key(String userAlias)
        throws IOException {
        crateAddRequestWithInvalidApiKey(userAlias);
        runAddRequest();
    }

    @When("^the InternalService requests to add the (\\w*) without an API key$")
    public void the_InternalService_requests_to_add_a_user_without_an_api_key(String userAlias)
        throws IOException {
        createAddRequestWithoutApiKey(userAlias);
        runAddRequest();
    }

    private void crateValidAddRequest(String userAlias)
        throws JsonProcessingException {
        UserDto newUser = scenarioContext.getExampleUser(userAlias);
        createRequest(userAlias, newUser, CORRECT_API_KEY);
    }

    private void crateAddRequestWithInvalidApiKey(String userAlias) throws JsonProcessingException {
        UserDto newUser = scenarioContext.getExampleUser(userAlias);
        createRequest(userAlias, newUser, WRONG_API_KEY);
    }

    private void createAddRequestWithoutApiKey(String userAlias)
        throws JsonProcessingException {
        UserDto newUser = scenarioContext.getExampleUser(userAlias);
        createRequest(userAlias, newUser, null);
    }

    private void createGetRequestWithoutAnApiKey(String userAlias)
        throws JsonProcessingException {
        createRequest(userAlias, null, null);
    }

    private void runAddRequest() throws IOException {
        runRequest(InternalServiceMock::defaultAddHandler);
    }

    private void runGetRequest() throws IOException {
        runRequest(InternalServiceMock::defaultGetHandler);
    }

    private <I, O> void runRequest(Function<InternalServiceMock, ApiGatewayHandler<I, O>> handlerProvider)
        throws IOException {

        InternalServiceMock internalService = new InternalServiceMock(scenarioContext.getDatabaseService());
        handlerSendsRequestAndUpdatesResponse(handlerProvider.apply(internalService));
    }

    private void createGetRequestWithInvalidApiKey(String userAlias)
        throws JsonProcessingException {
        createRequest(userAlias, null, WRONG_API_KEY);
    }

    private void createValidGetRequest(String userAlias)
        throws JsonProcessingException {
        createRequest(userAlias, null, CORRECT_API_KEY);
    }

    private <T> void createRequest(String userAlias, T body, String apiKey)
        throws JsonProcessingException {

        initializeContextRequestBuilderWithBody(body);
        scenarioContext.getRequestBuilder()
            .withHeaders(authorizationHeader(apiKey))
            .withPathParameters(validPathParameter(userAlias));
    }

    private Map<String, String> validPathParameter(String userAlias) {
        return Collections.singletonMap(GetUserHandlerForInternalService.USERNAME_PATH_PARAMETER,
            scenarioContext.getExampleUser(userAlias).getUsername());
    }

    private Map<String, String> authorizationHeader(String apiKey) {
        if (nonNull(apiKey)) {
            return Collections.singletonMap(ApiKeyValidation.AUTHORIZATION_HEADER, apiKey);
        } else {
            return null;
        }
    }
}
