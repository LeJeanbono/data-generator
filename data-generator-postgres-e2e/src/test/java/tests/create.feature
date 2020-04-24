Feature: Create

  Background:
    * url baseUrl
    * call read('classpath:utils/cleanAll.feature')

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

  Scenario: Create bad entity
    Given path '/test/datas/bad'
    And request {}
    When method post
    Then status 400
    And assert response.message == 'Bad.class does not exist, did you add @TestData ?'

  Scenario: Create many
    Given path '/test/datas/myEntity'
    And param number = 2
    And request {}
    When method post
    Then status 201
    Given path '/myentity'
    When method get
    Then status 200
    And assert response.page.totalElements == 2