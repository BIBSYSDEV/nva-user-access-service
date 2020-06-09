Feature: Role features


  Background:
    Given a database for users and roles
    And an authorized client

  Scenario: Authorized client creates Role
    When then authorized client sends a POST request
    And the request contains a JSON body with following key-value pairs
      | key      | value   |
      | rolename | theRole |
    Then a new role is stored in the database
    And the description of the role is returned to the authorized client

