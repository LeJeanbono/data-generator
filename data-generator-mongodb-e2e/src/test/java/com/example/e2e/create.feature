Feature: Create

  Background:
    * url baseUrl

  Scenario: Create
    Given path '/test/datas/myEntity'
    And request {}
    When method post
    Then status 201
    * def id = response.id
    * def name = response.name
    Given path '/myentity/' + id
    When method get
    Then status 200
    And match response.name == name