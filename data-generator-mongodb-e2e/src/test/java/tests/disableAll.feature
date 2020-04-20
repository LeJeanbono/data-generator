Feature: Test disable data-generator

  Background:
    * url baseUrl
    * call read('classpath:utils/cleanAll.feature')

  Scenario: Delete all datas
    Given path '/utils/config'
    And request { enabled:false }
    When method post
    Then status 204

    Given path '/test/datas'
    When method delete
    Then status 403

    Given path '/test/datas/dfdf'
    And request { sds:'sqdqs' }
    When method post
    Then status 403

    Given path '/test/datas/dfdf'
    When method get
    Then status 403

    Given path '/test/datas/dfdf/Dfdg'
    When method get
    Then status 403

    Given path '/test/datas/efdz/zeze'
    When method delete
    Then status 403