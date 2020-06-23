package features;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import no.unit.nva.database.DatabaseServiceImpl;
import no.unit.nva.database.DatabaseTest;

public class ScenarioCommons extends DatabaseTest {

    private final ScenarioContext scenarioContext;

    public ScenarioCommons(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("a database for users and roles")
    public void a_database_for_users_and_roles() {
        DynamoDBMapper mapper = new DynamoDBMapper(initializeTestDatabase());
        this.scenarioContext.setDatabaseService(new DatabaseServiceImpl(mapper));
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
