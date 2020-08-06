package features;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class InternalServiceAccessFt extends ScenarioTest {

    public InternalServiceAccessFt(ScenarioContext scenarioContext) {
        super(scenarioContext);
    }

    @Given("an Internal Service with an API key")
    public void an_Internal_Service_with_an_API_key() {
        //DO NOTHING
    }

    @When("the InternalService requests to get the ExistingUser using a valid API key")
    public void the_InternalService_requests_to_get_the_ExistingUser_using_a_valid_API_key() {
        throw new io.cucumber.java.PendingException();
    }
}
