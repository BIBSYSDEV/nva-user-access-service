Feature: Users

  Background:
    Given a database for users and roles
    And an authorized client

  Scenario: Authorized client adds user
    Given that a user entry with the username "someone@institution" does not exist in the database
    And the authorized client forms a "POST" request
    And the request contains a JSON body with following key-value pairs
      | key         | value               |
      | username    | someone@institution |
      | institution | institution         |
    And the request body also contains a list of roles with the following role-names
      | role-name |
      | RoleA     |
      | RoleB     |
    When the authorized client sends the request
    Then the handler returns a user object
    And the user object contains all the aforementioned information



