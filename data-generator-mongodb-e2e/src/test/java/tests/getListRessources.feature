Feature: Get List ressources

  Background:
    * url baseUrl
    * call read('classpath:utils/cleanAll.feature')

  Scenario: Get List ressources
    Given path '/test/datas'
    When method get
    Then status 200
    And match response == '#[1]'
    And assert response[0] === 'myEntity'

  Scenario: Get List ressources with plural
    Given path '/utils/config'
    And request { pluralRessources:true }
    When method post
    Then status 204

    Given path '/test/datas'
    When method get
    Then status 200
    And match response == '#[1]'
    And assert response[0] === 'myEntitys'
