# data-generator
[![CircleCI](https://circleci.com/gh/LeJeanbono/data-generator.svg?style=svg)](https://circleci.com/gh/LeJeanbono/data-generator)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f018f2014d6549a192e78f7476d480c7)](https://www.codacy.com/manual/jean.michel.lec/data-generator?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=LeJeanbono/data-generator&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/LeJeanbono/data-generator/branch/master/graph/badge.svg)](https://codecov.io/gh/LeJeanbono/data-generator)
## Getting started
### Gradle
```groovy
implementation 'com.cooperl:data-generator-core:0.3.1'
implementation 'com.cooperl:data-generator-mongodb:0.3.1'
```
### Maven
```xml
<!-- replace here with the latest version -->
<dependency>
    <groupId>com.cooperl</groupId>
    <artifactId>data-generator-core</artifactId>
    <version>0.3.1</version>
</dependency>
<dependency>
    <groupId>com.cooperl</groupId>
    <artifactId>data-generator-mongodb</artifactId>
    <version>0.3.1</version>
</dependency>
```
### Activation
In your `application.properties` :
```
datagenerator.pluralRessources=true
```
### Use
Add `@TestData` on bean to give you access at the API 
```java
@TestData
public class MyEntity {
    @Id
    private String id;
    private String name;
}
```