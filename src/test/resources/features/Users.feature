Feature: Users

  Background:
    Given a database for users and roles
    And an authorized client

  Scenario: Authorized client adds a new user
    Given  that a user entry with the username "someone@institution" does not exist in the database
    And the authorized client forms a "POST" request
    And the request contains a JSON body with following key-value pairs
      | key         | value               |
      | username    | someone@institution |
      | institution | institution         |
    And the request body also contains a list of roles with the following role-names
      | role-name |
      | RoleA     |
      | RoleB     |
    When the authorized client sends the request to add a new User
    Then a user description is returned
    And the user object contains all the aforementioned information

  Scenario: Authorized client gets an existing user
    Given  that a user entry with the username "someone@institution" exists in the database
    And the authorized client forms a "GET" request
    And the request has the following path parameters:
      | parameter | value               |
      | user      | someone@institution |
    When the authorized client sends the request to read the user
    Then a user description is returned

  Scenario: Authorized client gets a non-existing user
    Given that a user entry with the username "someone@institution" does not exist in the database
    And the authorized client forms a "GET" request
    And the request has the following path parameters:
      | parameter | value               |
      | username  | someone@institution |
    When the authorized client sends the request to read the user
    Then a NotFound message is returned

  Scenario:  Authorized client updates existing user
    Given that a user entry with the username "someone@institution" exists in the database
    And the user entry contains a list of roles with the following role-names
      | role-name |
      | RoleA     |
    And the authorized client forms a "PUT" request
    And the request has the following path parameters:
      | parameter | value               |
      | username  | someone@institution |
    And the request contains a JSON body with following key-value pairs
      | key         | value               |
      | username    | someone@institution |
      | institution | institution         |
    And the request body also contains a list of roles with the following role-names
      | role-name |
      | RoleB     |
    When the authorized client sends the request to update the user
    Then the user entry is updated asynchronously
    And a Location header with the updated user URI is included in the response

  Scenario:Authorized client attempts to update non-existing user
    Given that a user entry with the username "someone@institution" does not exist in the database
    And the authorized client forms a "PUT" request
    And the request has the following path parameters:
      | parameter | value               |
      | username  | someone@institution |
    And the request contains a JSON body with following key-value pairs
      | key         | value               |
      | username    | someone@institution |
      | institution | institution         |
    And the request body also contains a list of roles with the following role-names
      | role-name |
      | RoleB     |
    When the authorized client sends the request to update the user
    Then a NotFound message is returned

  Scenario:Authorized client attempts to update existing user with malformed request
    Given that a user entry with the username "someone@institution" exists in the database
    And the authorized client forms a "PUT" request
    And the request has the following path parameters:
      | parameter | value               |
      | username  | someone@institution |
    And the request contains a malformed JSON body
    When the authorized client sends the request to update the user
    Then a BadRequest message is returned containing information about the invalid request


