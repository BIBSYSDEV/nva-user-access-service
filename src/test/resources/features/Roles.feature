Feature: Role features


  Background:
    Given a database for users and roles
    And an authorized client

  Scenario: Authorized client creates Role
    When the authorized client sends a "POST" request
    And the request contains a JSON body with following key-value pairs
      | key      | value   |
      | rolename | theRole |
    Then a new role is stored in the database
    And the description of the role is returned to the authorized client


  Scenario: Authorized client reads Role
    Given that there is a role with role-name "TheRole"
    When the authorized client sends a "GET" request with the following path parameters:
      | parameter | value   |
      | role      | TheRole |
    Then a role description is returned
    And the role description contains the following fields and respective values:
      | field    | fieldValue |
      | rolename | TheRole    |


