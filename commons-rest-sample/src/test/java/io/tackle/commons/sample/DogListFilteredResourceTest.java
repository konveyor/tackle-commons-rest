package io.tackle.commons.sample;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.tackle.commons.sample.entities.Dog;
import io.tackle.commons.testcontainers.KeycloakTestResource;
import io.tackle.commons.testcontainers.PostgreSQLDatabaseTestResource;
import io.tackle.commons.tests.SecuredResourceTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
public class DogListFilteredResourceTest extends SecuredResourceTest {

    @BeforeAll
    public static void init() {
        PATH = "/dog";
    }

    @Test
    public void testDogOrderedListEndpoint() {
        given()
            .accept(ContentType.JSON)
            .param("sort", "owner.name")
            .when().get(PATH)
            .then()
                .statusCode(200)
                .body("id", containsInRelativeOrder(2),
                        "name", containsInRelativeOrder("a"),
                        "race", containsInRelativeOrder("b"),
                        "owner.id", containsInRelativeOrder(1),
                        "owner.name", containsInRelativeOrder("c")
                );
    }

    @Test
    public void testDogOrderedHalListEndpoint() {
        given()
            .accept("application/hal+json")
            .param("sort", "-owner.name")
            .when().get(PATH)
            .then()
                .log().all()
                .statusCode(200)
                .body("_embedded.dog.id", containsInRelativeOrder(2),
                        "_embedded.dog.name", containsInRelativeOrder("a"),
                        "_embedded.dog.race", containsInRelativeOrder("b"),
                        "_embedded.dog.owner.name", containsInRelativeOrder("c"),
                        "_embedded._metadata.totalCount", is(3),
                        "_links.size()", is(4),
                        "_links.first.href", is("http://localhost:8081/dog?page=0&size=20&sort=-owner.name"),
                        "total_count", is(3)
                );
    }

    @Test
    public void testDogFilteredWrongParamListHalEndpoint() {
        // existing field without @Filterable annotation
        given()
                .accept("application/hal+json")
                .queryParam("race", "b")
                .when().get(PATH)
                .then()
                .statusCode(400);

        // wrong fields
        given()
                .accept("application/hal+json")
                .queryParam("wrong", "wrongAsWell")
                .when().get(PATH)
                .then()
                .statusCode(400);

        given()
                .accept("application/hal+json")
                .queryParam("owner.wrong", "wrongAsWell")
                .when().get(PATH)
                .then()
                .statusCode(400);
    }

    @Test
    public void testDogWeirdParameters() {
        given()
                .accept("application/hal+json")
                .queryParam("size", "-1")
                .queryParam("page", "-1")
                .when().get(PATH)
                .then()
                .statusCode(200)
                .body("_embedded.dog.size()", is(3),
                        "_embedded.dog[0]._links.size()", is(5),
                        "_embedded.dog[0]._links.self.href", is("http://localhost:8081/dog/2"),
                        "_links.size()", is(4));
    }

    @Test
    public void testDogPaginationWithPrevAndNextLinks() {
        given()
                .accept("application/hal+json")
                .queryParam("size", "1")
                .queryParam("page", "1")
                .when().get(PATH)
                .then()
                .log().all()
                .statusCode(200)
                .body("_embedded.dog.size()", is(1),
                        "_embedded.dog[0].id", is(4),
                        "_embedded.dog[0].name", is("e"),
                        "_embedded.dog[0]._links.size()", is(5),
                        "_embedded.dog[0]._links.self.href", is("http://localhost:8081/dog/4"),
                        "_embedded._metadata.totalCount", is(3),
                        "total_count", is(3),
                        "_links.size()", is(6));
    }

    @Test
    public void testDogFilterParameters() {
        given()
                .accept("application/hal+json")
                .queryParam("name", "a")
                .queryParam("name", "e")
                .queryParam("owner.name", "c")
                .when().get(PATH)
                .then()
                .statusCode(200)
                .body("_embedded.dog.size()", is(1),
                        "_embedded.dog[0]._links.size()", is(5),
                        "_embedded.dog[0]._links.self.href", is("http://localhost:8081/dog/2"),
                        "_links.size()", is(4));
    }

    @Test
    @DisabledOnNativeImage
    public void testDogCreateUpdateAndDeleteEndpoint() {
        testDogCreateUpdateAndDeleteEndpoint(false);
    }

    protected void testDogCreateUpdateAndDeleteEndpoint(boolean nativeExecution) {
        final String name = "Name";
        final String race = "Race";
        Dog dog = new Dog();
        dog.name = name;
        dog.race = race;

        Response response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(dog)
                .when().post(PATH)
                .then()
                .statusCode(201).extract().response();

        assertEquals(name, response.path("name"));
        assertEquals(race, response.path("race"));
        assertEquals("alice", response.path("createUser"));
        assertEquals("alice", response.path("updateUser"));

        Long dogId = Long.valueOf(response.path("id").toString());

        final String newName = "Yet another different name";
        dog.name = newName;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(dog)
                .pathParam("id", dogId)
                .when().put(PATH + "/{id}")
                .then().statusCode(204);

        given()
                .accept("application/json")
                .pathParam("id", dogId)
                .when().get(PATH + "/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .body("name", is(newName),
                        "race", is(race));

        if (!nativeExecution) {
            Dog updatedDogFromDb = dog.findById(dogId);
            assertEquals(newName, updatedDogFromDb.name);
            assertEquals(race, updatedDogFromDb.race);
            assertNotNull(updatedDogFromDb.createTime);
            assertNotNull(updatedDogFromDb.updateTime);
        }

        given()
                .pathParam("id", dogId)
                .when().delete(PATH + "/{id}")
                .then().statusCode(204);

        given()
                .accept("application/json")
                .pathParam("id", dogId)
                .when().get(PATH + "/{id}")
                .then()
                .log().all()
                .statusCode(404);

    }

}