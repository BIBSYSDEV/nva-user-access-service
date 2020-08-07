Feature: Provide internal access to authorization services using static API keys


  Background:
    Given a Database for users and roles
    Given an Internal Service with an API key
    And an ExistingUser with username "ExistingUser" that exists in the Database
    And the ExistingUser belongs to the institution "Institution"
    And the ExistingUser has the roles:
      | roles |
      | roleA |


    And a non-existing NewUser with username "NewUser" that does not yet exist in the database
    And NewUser is specified that it will have the roles:
      | roles |
      | RoleA |

  Scenario: Service with valid API key reads existing User details.
    When the InternalService requests to get the ExistingUser using a valid API key
    Then the response object contains the UserDescription of the ExistingUser

  Scenario: Service with invalid API key attempts to read User details
    When the InternalService requests to get the ExistingUser using an invalid API key
    Then a Forbidden message is returned

  Scenario: Service with invalid API-key attempts to read User details
    When the InternalService requests to get the ExistingUser without an API key
    Then a Forbidden message is returned

  Scenario: Service with valid API key adds  new User details.
    When the InternalService requests to add the NewUser using a valid API key
    Then the response object contains the UserDescription of the NewUser

  Scenario: Service with invalid API key attempts to add a new User
    When the InternalService requests to add the NewUser using an invalid API key
    Then a Forbidden message is returned


  Scenario: Service without an API key attempts to add a new User
    When the InternalService requests to add the NewUser without an API key
    Then a Forbidden message is returned