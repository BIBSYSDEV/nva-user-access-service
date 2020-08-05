Feature: Provide internal access to authorization services using static API keys


  Background:
    Given a Database for users and roles
    Given an Internal Service with an API key
    And an ExistingUser with username "ExistingUser" that exists in the Database
    And the ExistingUser belongs to the institution "Institution"
    And the ExistingUser has the roles:
    |roles|
    |roleA|

  Scenario: Service with static API key reads existing User details.
    When the InternalService requests to get the ExistingUser using a valid API key
    Then the response object contains the UserDescription of the ExistingUser

