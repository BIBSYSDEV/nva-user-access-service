package features;

import static java.util.Objects.nonNull;
import static no.unit.nva.handlers.InternalServiceMock.CORRECT_API_KEY;
import static no.unit.nva.handlers.InternalServiceMock.WRONG_API_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.ApiGatewayHandler;
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
        runGetRequest(createValidGetRequest(userAlias));
    }

    @When("^the InternalService requests to get the (\\w*) using an invalid API key$")
    public void the_InternalService_requests_to_get_the_user_using_a_invalid_API_key(String userAlias)
        throws IOException {
        runGetRequest(createGetRequestWithInvalidApiKey(userAlias));
    }

    @When("^the InternalService requests to get the (\\w*) without an API key$")
    public void the_InternalService_requests_to_get_the_user_without_an_api_key(String userAlias)
        throws IOException {
        runGetRequest(createGetRequestWithoutAnApiKey(userAlias));
    }

    @When("^the InternalService requests to add the (\\w*) using a valid API key$")
    public void the_InternalService_requests_to_add_a_user_using_a_valid_API_key(String userAlias)
        throws IOException {
        // Write code here that turns the phrase above into concrete actions
        runAddRequest(crateValidAddRequest(userAlias));
    }

    @When("^the InternalService requests to add the (\\w*) using an invalid API key$")
    public void the_InternalService_requests_to_add_a_user_using_a_invalid_API_key(String userAlias)
        throws IOException {
        // Write code here that turns the phrase above into concrete actions
        runAddRequest(crateInValidAddRequest(userAlias));
    }

    @When("^the InternalService requests to add the (\\w*) without an API key$")
    public void the_InternalService_requests_to_add_a_user_without_an_api_key(String userAlias)
        throws IOException {
        // Write code here that turns the phrase above into concrete actions
        runAddRequest(createAddRequestWithoutApiKey(userAlias));
    }

    private HandlerRequestBuilder<Map<String, Object>> crateValidAddRequest(String userAlias)
        throws JsonProcessingException {
        UserDto newUser = scenarioContext.getExampleUser(userAlias);
        return createRequest(userAlias, newUser, CORRECT_API_KEY);
    }

    private HandlerRequestBuilder<Map<String, Object>> crateInValidAddRequest(String userAlias)
        throws JsonProcessingException {
        UserDto newUser = scenarioContext.getExampleUser(userAlias);
        return createRequest(userAlias, newUser, WRONG_API_KEY);
    }

    private HandlerRequestBuilder<Map<String, Object>> createAddRequestWithoutApiKey(String userAlias)
        throws JsonProcessingException {
        UserDto newUser = scenarioContext.getExampleUser(userAlias);
        return createRequest(userAlias, newUser, null);
    }

    private HandlerRequestBuilder<Map<String, Object>> createGetRequestWithoutAnApiKey(String userAlias)
        throws JsonProcessingException {
        return createRequest(userAlias, null, null);
    }

    private void runAddRequest(HandlerRequestBuilder<Map<String, Object>> requestBuilder) throws IOException {
        runRequest(requestBuilder, InternalServiceMock::defaultAddHandler);
    }

    private void runGetRequest(HandlerRequestBuilder<Map<String, Object>> requestBuilder) throws IOException {
        runRequest(requestBuilder, InternalServiceMock::defaultGetHandler);
    }

    private <I, O> void runRequest(HandlerRequestBuilder<Map<String, Object>> requestBuilder,
                                   Function<InternalServiceMock, ApiGatewayHandler<I, O>> handlerProvider)
        throws IOException {

        InternalServiceMock internalService = new InternalServiceMock(scenarioContext.getDatabaseService());
        scenarioContext.setRequestBuilder(requestBuilder);
        handlerSendsRequestAndUpdatesResponse(handlerProvider.apply(internalService));
    }

    private HandlerRequestBuilder<Map<String, Object>> createGetRequestWithInvalidApiKey(String userAlias)
        throws JsonProcessingException {
        return createRequest(userAlias, null, WRONG_API_KEY);
    }

    private HandlerRequestBuilder<Map<String, Object>> createValidGetRequest(String userAlias)
        throws JsonProcessingException {
        return createRequest(userAlias, null, CORRECT_API_KEY);
    }

    private <T> HandlerRequestBuilder<Map<String, Object>> createRequest(String userAlias, T body, String apiKey)
        throws JsonProcessingException {

        Map<String, Object> bodyMap = convertObjectToMap(body);

        return new HandlerRequestBuilder<Map<String, Object>>(JsonUtils.objectMapper)
            .withHeaders(authorizationHeader(apiKey))
            .withPathParameters(validPathParameter(userAlias))
            .withBody(bodyMap);
    }

    private <T> Map<String, Object> convertObjectToMap(T body) {
        if (nonNull(body)) {
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {};
            return JsonUtils.objectMapper.convertValue(body, typeReference);
        }
        return null;
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
