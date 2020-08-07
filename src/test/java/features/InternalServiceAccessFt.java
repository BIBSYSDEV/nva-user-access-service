package features;

import static java.util.Objects.nonNull;
import static no.unit.nva.handlers.InternalServiceMock.CORRECT_API_KEY;
import static no.unit.nva.handlers.InternalServiceMock.WRONG_API_KEY;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import no.unit.nva.handlers.ApiKeyValidation;
import no.unit.nva.handlers.GetUserHandlerForInternalService;
import no.unit.nva.handlers.InternalServiceMock;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.utils.JsonUtils;

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
        runRequest(createValidRequest(userAlias));
    }

    @When("^the InternalService requests to get the (\\w*) using an invalid API key$")
    public void the_InternalService_requests_to_get_the_user_using_a_invalid_API_key(String userAlias)
        throws IOException {
        runRequest(createRequestWithInvalidApiKey(userAlias));
    }

    @When("^the InternalService requests to get the (\\w*) without an API key")
    public void the_InternalService_requests_to_get_the_user_without_an_api_key(String userAlias)
        throws IOException {
        runRequest(createRequestWithoutAnApiKey(userAlias));
    }

    private HandlerRequestBuilder<Map<String, Object>> createRequestWithoutAnApiKey(String userAlias) {
        return createRequest(userAlias, null);
    }

    private void runRequest(HandlerRequestBuilder<Map<String, Object>> inValidRequest) throws IOException {
        InternalServiceMock internalService = new InternalServiceMock(scenarioContext.getDatabaseService());
        HandlerRequestBuilder<Map<String, Object>> request = inValidRequest;
        scenarioContext.setRequestBuilder(request);
        handlerSendsRequestAndUpdatesResponse(internalService.defaultHandler());
    }

    private HandlerRequestBuilder<Map<String, Object>> createRequestWithInvalidApiKey(String userAlias) {
        return createRequest(userAlias, WRONG_API_KEY);
    }

    private HandlerRequestBuilder<Map<String, Object>> createValidRequest(String userAlias) {
        return createRequest(userAlias, CORRECT_API_KEY);
    }

    private HandlerRequestBuilder<Map<String, Object>> createRequest(String userAlias, String apiKey) {
        return new HandlerRequestBuilder<Map<String, Object>>(JsonUtils.objectMapper)
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
