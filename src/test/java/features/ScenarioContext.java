package features;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import java.util.Map;
import no.unit.nva.database.DatabaseServiceImpl;

public class ScenarioContext {

    private Map<String, Object> requestBody;
    private DynamoDBMapper dynamoDBMapper;
    private DatabaseServiceImpl databaseService;

    public Map<String, Object> getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Map<String, Object> requestBody) {
        this.requestBody = requestBody;
    }

    public DatabaseServiceImpl getDatabaseService() {
        return databaseService;
    }

    public void setDatabaseService(DatabaseServiceImpl databaseService) {
        this.databaseService = databaseService;
    }
}
