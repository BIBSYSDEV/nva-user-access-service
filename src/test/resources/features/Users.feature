Feature: Users

  Scenario: get user details
    Given a user with username "someone@institution.com"
    When handler receives a request for accessing user details
    Then the handler returns the user details


