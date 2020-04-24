Feature: Get

  Background:
    * url baseUrl
    * call read('classpath:utils/cleanAll.feature')

  Scenario: Get resources
    Given path '/myentity'
    And request {id: 'myId', name : 'TOTO'}
    When method post
    Then status 201
    Given path '/test/datas/myEntity/myId'
    When method get
    Then status 200
    And match response.id == 'myId'
    And match response.name == 'TOTO'

  Scenario: Get bad id
    Given path '/test/datas/myEntity/123'
    And request {}
    When method get
    Then status 404

  Scenario: Get bad entity
    Given path '/test/datas/bad/123'
    And request {}
    When method get
    Then status 400
    And assert response.message == 'Bad.class does not exist, did you add @TestData ?'