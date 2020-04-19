Feature: Delete

  Background:
    * url baseUrl
    * call read('classpath:utils/cleanAll.feature')

  Scenario: Delete resources
    Given path '/myentity'
    And request {id: 'myId', name : 'TOTO'}
    When method post
    Then status 201
    Given path '/test/datas/myEntity/myId'
    When method delete
    Then status 204
    Given path '/myentity/myId'
    When method get
    Then status 404

  Scenario: Create bad entity
    Given path '/test/datas/bad/123'
    And request {}
    When method delete
    Then status 400
    And assert response.message == 'Bad.class does not exist, did you add @TestData ?'