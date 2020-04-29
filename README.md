# data-generator
[![CircleCI](https://img.shields.io/circleci/build/github/LeJeanbono/data-generator)](https://circleci.com/gh/LeJeanbono/data-generator)
[![Codacy Badge](https://img.shields.io/codacy/grade/f018f2014d6549a192e78f7476d480c7)](https://www.codacy.com/manual/jean.michel.lec/data-generator?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=LeJeanbono/data-generator&amp;utm_campaign=Badge_Grade)
[![codecov](https://img.shields.io/codecov/c/github/LeJeanbono/data-generator)](https://codecov.io/gh/LeJeanbono/data-generator)
## Getting started
### Gradle
```groovy
implementation 'com.github.lejeanbono:data-generator-core:0.3.1'
implementation 'com.github.lejeanbono:data-generator-mongodb:0.3.1'
```
### Maven
```xml
<!-- replace here with the latest version -->
<dependency>
    <groupId>com.github.lejeanbono</groupId>
    <artifactId>data-generator-core</artifactId>
    <version>0.3.1</version>
</dependency>
<dependency>
    <groupId>com.github.lejeanbono</groupId>
    <artifactId>data-generator-mongodb</artifactId>
    <version>0.3.1</version>
</dependency>
```
### Activation
Add `@EnableDataGenerator` and import `DataGeneratorMongoDBConfig`
```
@EnableDataGenerator
@SpringBootApplication
@Import(DataGeneratorMongoDBConfig.class)
public class EApplication {
    public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```
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
