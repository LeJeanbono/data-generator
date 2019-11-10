Feature: Test delete

  Background:
    * url baseUrl

  Scenario: Delete all datas

    Given path 'test/datas'
    When method delete
    Then status 204