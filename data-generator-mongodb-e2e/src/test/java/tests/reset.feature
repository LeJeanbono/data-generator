Feature: Test delete all

  Background:
    * url baseUrl
    * call read('classpath:utils/cleanAll.feature')

  Scenario: Delete all datas
    Given path '/myentity'
    And request {name : 'TOTO'}
    When method post
    Then status 201
    * def getUrl = responseHeaders['Location'][0]
    Given url getUrl
    When method get
    Then status 200
    And assert response.name == 'TOTO'
    * url baseUrl
    Given path '/test/datas'
    When method delete
    Then status 204
    Given path '/myentity'
    When method get
    Then status 200
    And match response.page.totalElements == 0