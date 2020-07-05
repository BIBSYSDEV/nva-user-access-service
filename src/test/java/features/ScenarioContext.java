package features;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
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

    public HandlerRequestBuilder<Map<String, Object>> getRequestBuilder() {
        if (isNull(requestBuilder)) {
            requestBuilder = new HandlerRequestBuilder<>(JsonUtils.objectMapper);
        }
        return requestBuilder;
    }

    public void setRequestBuilder(HandlerRequestBuilder<Map<String, Object>> requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public DatabaseServiceImpl getDatabaseService() {
        return databaseService;
    }

    public void setDatabaseService(DatabaseServiceImpl databaseService) {
        this.databaseService = databaseService;
    }

    public <T> GatewayResponse<T> getApiGatewayResponse(Class<T> responseBodyClass) throws JsonProcessingException {
        JavaType typeRef = JsonUtils.objectMapper.getTypeFactory()
            .constructParametricType(GatewayResponse.class, responseBodyClass);
        return JsonUtils.objectMapper.readValue(requestResponse, typeRef);
    }

    public String getResponseString() throws JsonProcessingException {
        return this.requestResponse;
    }

    public <T> T getResponseBody(Class<T> clazz) throws JsonProcessingException {
        return  getApiGatewayResponse(clazz).getBodyObject(clazz);
    }

    public void setRequestResponse(String requestResponse) {
        this.requestResponse = requestResponse;
    }

    public Supplier<? extends ApiGatewayHandler<?, ?>> getHandlerSupplier() {
        return Optional.ofNullable(handlerSupplier)
            .orElseThrow(() -> new IllegalStateException(API_GATEWAY_SUPPLIER_HAS_NOT_BEEN_SET));
    }

    public void setHandlerSupplier(Supplier<? extends ApiGatewayHandler<?, ?>> handlerSupplier) {
        this.handlerSupplier = handlerSupplier;
    }
}
