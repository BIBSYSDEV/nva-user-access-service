Feature: Provide internal access to authorization services using static API keys


  Background:
    Given an Internal Service with static API key

  Scenario: Service with static API key reads existing User details.
    Given  that a User with username "someone@institution" exists in the database
    And the Internal Service forms a "GET" request
    And the "GET" request contains a valid API-key
    And the request has the following path parameters:
      | parameter | value               |
      | username  | someone@institution |
    When the Internal Service sends the request to read the user
    Then a user description is returned

  Scenario: Service with static API key asks for non-existing User details.
    Given  that a User with username "someone@institution" does not exist in the database
    And the Internal Service forms a "GET" request
    And the "GET" request contains a valid API-key
    And the request has the following path parameters:
      | parameter | value               |
      | username  | someone@institution |
    When the Internal Service sends the request to read the user
    Then a NotFound message is returned

  Scenario Outline: Service with static API key sends request with invalid API-key.
    Given the Internal Service forms a <METHOD> request
    And the <METHOD> request contains an invalid API-key
    When the Internal Service sends the request to read the user
    Then a "Forbidden" message is returned
    Examples:
      | METHOD |
      | GET    |
      | POST   |

  Scenario Outline: Internal Service adds a new user with a NonAdminRole Role
    Given  that a User with username "someone@institution" does not exist in the database
    And the Internal Service forms a "POST" request
    And the "POST" request contains a valid API-key
    And the request contains a JSON body with following key-value pairs
      | key         | value               |
      | username    | someone@institution |
      | institution | institution         |
      | type        | User                |
    And the request body also contains a list of roles with the following role-names
      | role-name      |
      | <NonAdminRole> |
    When the Internal Service sends the request to add a new User
    Then a success message is returned
    And a Location header with the user URI is included in the response

    Examples:
      | NonAdminRole |
      | Creator      |
      | User         |


  Scenario Outline: Internal Service cannot overwrite an existing user
    Given  that a User with username "someone@institution" does not exist in the database
    And the Internal Service forms a "POST" request
    And the "POST" request contains a valid API-key
    And the request contains a JSON body with following key-value pairs
      | key         | value               |
      | username    | someone@institution |
      | institution | institution         |
      | type        | User                |
    And the request body also contains a list of roles with the following role-names
      | role-name      |
      | <NonAdminRole> |
    When the Internal Service sends the request to add a new User
    Then a success message is returned
    And a Location header with the user URI is included in the response

    Examples:
      | NonAdminRole |
      | Creator      |
      | User         |

