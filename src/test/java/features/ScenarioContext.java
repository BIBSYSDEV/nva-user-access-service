package features;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.JavaType;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.JsonUtils;

public class ScenarioContext {

    public static final String API_GATEWAY_SUPPLIER_HAS_NOT_BEEN_SET = "ApiGatewayHandler supplier has not been set";
    private HandlerRequestBuilder<Map<String, Object>> requestBuilder;
    private String requestResponse;
    private DatabaseServiceImpl databaseService;
    private Supplier<? extends ApiGatewayHandler<?, ?>> handlerSupplier;

    protected HandlerRequestBuilder<Map<String, Object>> getRequestBuilder() {
        if (isNull(requestBuilder)) {
            requestBuilder = new HandlerRequestBuilder<>(JsonUtils.objectMapper);
        }
        return requestBuilder;
    }

    protected void setRequestBuilder(HandlerRequestBuilder<Map<String, Object>> requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    protected DatabaseServiceImpl getDatabaseService() {
        return databaseService;
    }

    protected void setDatabaseService(DatabaseServiceImpl databaseService) {
        this.databaseService = databaseService;
    }

    protected <T> GatewayResponse<T> getApiGatewayResponse(Class<T> responseBodyClass) throws IOException {
        JavaType typeRef = JsonUtils.objectMapper.getTypeFactory()
            .constructParametrizedType(GatewayResponse.class, GatewayResponse.class, responseBodyClass);
        return JsonUtils.objectMapper.readValue(requestResponse, typeRef);
    }

    protected <T> T getResponseBody(Class<T> requestBodyClass) throws IOException {
        return getApiGatewayResponse(requestBodyClass).getBodyObject(requestBodyClass);
    }

    protected void setRequestResponse(String requestResponse) {
        this.requestResponse = requestResponse;
    }

    protected void setHandlerSupplier(Supplier<? extends ApiGatewayHandler<?, ?>> handlerSupplier) {
        this.handlerSupplier = handlerSupplier;
    }
}
