Feature: Test delete all

  Background:
    * url baseUrl

  Scenario: Delete all datas
    Given path 'myentity'
    And request {name : 'TOTO'}
    When method post
    Then status 201
    Given path 'myentity'
    When method get
    Then status 200
    And assert response._embedded.myentity[0].name == 'TOTO'
    Given path 'test/datas'
    When method delete
    Then status 204
    Given path 'myentity'
    When method get
    Then status 200
    And match response._embedded.myentity == '#[0]'