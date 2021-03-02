# commons-rest-integration-tests project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/commons-rest-integration-tests-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

## Related guides

- RESTEasy JAX-RS ([guide](https://quarkus.io/guides/rest-json)): REST endpoint framework implementing JAX-RS and more

## Provided examples

### RESTEasy JAX-RS example

REST is easy peasy with this Hello World RESTEasy resource.

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)

### RESTEasy JSON serialisation using Jackson

This example demonstrate RESTEasy JSON serialisation by letting you list, add and remove quark types from a list. Quarked!

[Related guide section...](https://quarkus.io/guides/rest-json#creating-your-first-json-rest-service)

# TIP

Getting a

```
HTTP Request to /dog failed, error id: b2589ba3-4a4a-41a8-9f03-6668bdb9278e-1: org.jboss.resteasy.spi.UnhandledException: com.fasterxml.jackson.databind.JsonMappingException: RESTEASY012020: Discovery failed for method io.tackle.commons.sample.resources.DogListFilteredResource.list: RESTEASY012010: Cannot guess type for Response.
```

exception?  
Check the `entityClassName` value for the `@LinkResource` annotation in the `FooListFilteredResource.list` method.

# Coverage

From project root `$  mvn clean test -Pjacoco && mvn jacoco:restore-instrumented-classes@default-restore-instrumented-classes jacoco:report-aggregate@report-aggregate -Pjacoco`  
and then open the report from `commons-rest-sample/target/site/jacoco-aggregate/index.html`
