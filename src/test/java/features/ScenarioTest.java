package features;

import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.datatable.DataTable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.intefaces.WithEnvironment;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.utils.JsonUtils;

public class ScenarioTest implements WithEnvironment {

    public static final int IGNORE_HEADER_ROW = 1;
    protected final ScenarioContext scenarioContext;

    protected ScenarioTest(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    protected static DataTable ignoreHeadersRow(DataTable datatable) {
        return datatable.rows(IGNORE_HEADER_ROW);
    }

    protected static TypeReference<Map<String, Object>> createRequestBuilderTypeRef() {
        return new TypeReference<>() {};
    }

    protected static <T> T readRequestBody(HandlerRequestBuilder<Map<String, Object>> requestBuilder,
                                           Class<T> requestBodyClass)
        throws JsonProcessingException {
        Map<String, Object> bodyMap = requestBuilder.getBody(createRequestBuilderTypeRef());
        return JsonUtils.objectMapper.convertValue(bodyMap, requestBodyClass);
    }

    protected static <T> Map<String, Object> prepareRequestBody(T requestObject) {
        return JsonUtils.objectMapper.convertValue(requestObject, createRequestBuilderTypeRef());
    }

    protected <I, O> void handlerSendsRequestAndUpdatesResponse(ApiGatewayHandler<I, O> handler) throws IOException {
        InputStream request = buildRequestInputStream();
        ByteArrayOutputStream outputStream = invokeHandlerWithRequest(handler, request);
        scenarioContext.setRequestResponse(outputStream.toString());
    }

    protected HandlerRequestBuilder<Map<String, Object>> getRequestBuilder() {
        return scenarioContext.getRequestBuilder();
    }

    protected DatabaseServiceImpl getDatabaseService() {
        return scenarioContext.getDatabaseService();
    }

    private <I, O> ByteArrayOutputStream invokeHandlerWithRequest(
        ApiGatewayHandler<I, O> handler,
        InputStream request) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        handler.handleRequest(request, outputStream,  mock(Context.class));
        return outputStream;
    }

    private InputStream buildRequestInputStream() throws JsonProcessingException {
        return scenarioContext.getRequestBuilder().build();
    }

    protected void setRequestBuilder(HandlerRequestBuilder<Map<String, Object>> requestBuilder) {
        scenarioContext.setRequestBuilder(requestBuilder);
    }

    protected <I> I getResponseBody(Class<I> requestBodyClass) throws IOException {
        return scenarioContext.getResponseBody(requestBodyClass);
    }
}
