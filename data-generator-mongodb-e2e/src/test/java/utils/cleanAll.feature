@ignore
Feature: Clean All

  Background:
    * url baseUrl

  Scenario: Delete all datas
    Given path '/utils/cleanAll'
    When method delete
    Then status 204