package io.tackle.commons.issues;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.tackle.commons.sample.entities.Dog;
import io.tackle.commons.sample.entities.Person;
import io.tackle.commons.testcontainers.KeycloakTestResource;
import io.tackle.commons.testcontainers.PostgreSQLDatabaseTestResource;
import io.tackle.commons.tests.SecuredResourceTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

// https://github.com/konveyor/tackle-controls/issues/9
@QuarkusTest
@QuarkusTestResource(value = PostgreSQLDatabaseTestResource.class,
        initArgs = {
                @ResourceArg(name = PostgreSQLDatabaseTestResource.DB_NAME, value = "sample_db"),
                @ResourceArg(name = PostgreSQLDatabaseTestResource.USER, value = "sample_user"),
                @ResourceArg(name = PostgreSQLDatabaseTestResource.PASSWORD, value = "sample_pwd")
        }
)
@QuarkusTestResource(value = KeycloakTestResource.class,
        initArgs = {
                @ResourceArg(name = KeycloakTestResource.IMPORT_REALM_JSON_PATH, value = "keycloak/import-realm.json"),
                @ResourceArg(name = KeycloakTestResource.REALM_NAME, value = "quarkus")
        }
)
public class TackleControlsIssue9Test extends SecuredResourceTest {

    @Test
    public void testIssuesTackleControlsIssue9() {
        Person owner = new Person();
        owner.name = "Owner";
        Response ownerResponse = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(owner)
                .when().post("/person")
                .then()
                .statusCode(201).extract().response();
        Long ownerId = Long.valueOf(ownerResponse.path("id").toString());

        Dog dog = new Dog();
        dog.name = "Dog";
        dog.owner = ownerResponse.as(Person.class);
        Response dogResponse = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(dog)
                .when().post("/dog")
                .then()
                .statusCode(201).extract().response();
        assertEquals("Owner", dogResponse.path("owner.name").toString());
        Long dogId = Long.valueOf(dogResponse.path("id").toString());

        given()
            .pathParam("id", ownerId)
            .when().delete("/person/{id}")
            .then().statusCode(204);

        given()
            .accept("application/json")
            .queryParam("name", "dog")
            .when().get("/dog")
            .then()
            .log().all()
            .statusCode(200)
            .body("name[0]", is("Dog"),
                    "owner[0]", is(emptyOrNullString()),
                    "id[0]", is(dogId.intValue()),
                    "size()", is(1));
    }
}
