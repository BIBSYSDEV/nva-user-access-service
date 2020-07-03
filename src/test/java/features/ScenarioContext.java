package features;

import static java.util.Objects.isNull;

import java.util.Map;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.utils.JsonUtils;

public class ScenarioContext {

    private HandlerRequestBuilder<Map<String,Object>> requestBuilder;
    private String requestResponse;
    private DatabaseServiceImpl databaseService;

    public HandlerRequestBuilder<Map<String, Object>> getRequestBuilder() {
        if(isNull(requestBuilder)){
            requestBuilder = new HandlerRequestBuilder<>(JsonUtils.objectMapper);
        }
        return requestBuilder;
    }

    public void setRequestBuilder(HandlerRequestBuilder<Map<String,Object>> requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public DatabaseServiceImpl getDatabaseService() {
        return databaseService;
    }

    public void setDatabaseService(DatabaseServiceImpl databaseService) {
        this.databaseService = databaseService;
    }

    public String getRequestResponse() {
        return requestResponse;
    }

    public void setRequestResponse(String requestResponse) {
        this.requestResponse = requestResponse;
    }
}
