Feature: Get All

  Background:
    * url baseUrl
    * call read('classpath:utils/cleanAll.feature')

  Scenario: Get All resources
    Given path '/myentity'
    And request {id: 'myId', name : 'TOTO'}
    When method post
    Then status 201
    Given path '/myentity'
    And request {id: 'myId2', name : 'TITI'}
    When method post
    Then status 201
    Given path '/test/datas/myEntity'
    When method get
    Then status 200
    And match response == '#[2]'
    And match response[0].name == 'TOTO'
    And match response[1].name == 'TITI'

  Scenario: Get bad entity
    Given path '/test/datas/bad'
    And request {}
    When method post
    Then status 400
    And assert response.message == 'Bad.class does not exist, did you add @TestData ?'