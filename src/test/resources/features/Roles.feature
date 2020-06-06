Feature: Role features


  Background:
    Given a database for users and roles
    And an authorized client


    Scenario:
      When then authorized client sends a createRole request with following parameters
         | rolename    |
         | theRolename |
      Then a new role is stored in the database
      And the description of the role is returned to the authorized client

