Feature: Role features


  Background:
    Given a database for users and roles
    And an authorized client

  Scenario: Authorized client creates Role
    Given the authorized client forms a "POST" request
    And the request contains a JSON body with following key-value pairs
      | key      | value   |
      | rolename | theRole |
    When the authorized client sends the request to add a new Role
    Then a new role is stored in the database
    And the description of the role is returned to the authorized client

  Scenario: Authorized client reads Role
    Given that there is a role with role-name "TheRole"
    And the authorized client forms a "GET" request
    And the request has the following path parameters:
      | parameter | value   |
      | role      | TheRole |
    When the authorized client sends the request to read the role
    Then a role description is returned

  Scenario: Authorized client tries to read non-existent Role
    Given that the authorized client intends to read a Role
    And that there is no role with role-name "TheRole"
    And the authorized client forms a "GET" request
    And the request has the following path parameters:
      | parameter | value   |
      | role      | TheRole |
    When the authorized client sends the request to read the role
    Then a NotFound message is returned
