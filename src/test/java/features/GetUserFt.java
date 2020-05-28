package features;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class GetUserFt {

    private String username;



    public GetUserFt(String username) {
        this.username = username;
    }

    @Given("a user with username (.*)")
    public void a_user_with_username(String username) {
        this.username = username;

    }

    @When("handler receives a request for accessing user details")
    public void handler_receives_a_request_for_accessing_user_details() {

    }

    @Then("the handler returns the user details")
    public void the_handler_returns_the_user_details() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
}
