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
import static org.hamcrest.Matchers.iterableWithSize;
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
                .body("id", containsInRelativeOrder(7, 3, 5, 6),
                        "name", containsInRelativeOrder("a"),
                        "color", containsInRelativeOrder("b"),
                        "owner.id", iterableWithSize(3),
                        "owner.id", containsInRelativeOrder(2, 2, 4),
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
                .body("_embedded.dog", iterableWithSize(4),
                "_embedded.dog.id", containsInRelativeOrder(6, 5, 7, 3),
                        "_embedded.dog.name", containsInRelativeOrder("K", "e", "g", "a"),
                        "_embedded.dog.color", containsInRelativeOrder("l", "f", "h", "b"),
                        // important check to validate the Dog without owner was in the list ('_embedded.dog' has size 4)
                        "_embedded.dog.owner.name", iterableWithSize(3),
                        "_embedded.dog.owner.name", containsInRelativeOrder("d", "c", "c"),
                        "_embedded._metadata.totalCount", is(4),
                        "_links.size()", is(4),
                        "_links.first.href", is("http://localhost:8081/dog?page=0&size=20&sort=-owner.name"),
                        "total_count", is(4)
                );
    }

    @Test
    public void testDogFilteredWrongParamListHalEndpoint() {
        // existing field without @Filterable annotation
        given()
                .accept("application/hal+json")
                .queryParam("color", "b")
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

        // 'id' must be a number
        given()
                .accept("application/hal+json")
                .queryParam("breed.id", "a")
                .when().get(PATH)
                .then()
                .statusCode(400);
    }

    @Test
    public void testDogPaginationWrongParameters() {
        given()
                .accept("application/hal+json")
                .queryParam("size", "-1")
                .queryParam("page", "-1")
                .when().get(PATH)
                .then()
                .statusCode(200)
                .body("_embedded.dog.size()", is(4),
                        "_embedded.dog[0]._links.size()", is(5),
                        "_embedded.dog[0]._links.self.href", is("http://localhost:8081/dog/3"),
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
                .statusCode(200)
                .body("_embedded.dog.size()", is(1),
                        "_embedded.dog[0].id", is(5),
                        "_embedded.dog[0].name", is("e"),
                        "_embedded.dog[0]._links.size()", is(5),
                        "_embedded.dog[0]._links.self.href", is("http://localhost:8081/dog/5"),
                        "_embedded._metadata.totalCount", is(4),
                        "total_count", is(4),
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
                        "_embedded.dog[0]._links.self.href", is("http://localhost:8081/dog/3"),
                        "_links.size()", is(4));
    }

    @Test
    public void testDogFilterByBreedId() {
        given()
                .accept("application/json")
                .queryParam("breed.id", "1")
                .when().get(PATH)
                .then()
                .statusCode(200)
                .body("", iterableWithSize(3),
                        "color", containsInRelativeOrder("b", "f", "l"));
    }

    @Test
    @DisabledOnNativeImage
    public void testDogCreateUpdateAndDeleteEndpoint() {
        testDogCreateUpdateAndDeleteEndpoint(false);
    }

    protected void testDogCreateUpdateAndDeleteEndpoint(boolean nativeExecution) {
        final String name = "Name";
        final String color = "Red";
        Dog dog = new Dog();
        dog.name = name;
        dog.color = color;

        Response response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(dog)
                .when().post(PATH)
                .then()
                .statusCode(201).extract().response();

        assertEquals(name, response.path("name"));
        assertEquals(color, response.path("color"));
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
                        "color", is(color));

        if (!nativeExecution) {
            Dog updatedDogFromDb = dog.findById(dogId);
            assertEquals(newName, updatedDogFromDb.name);
            assertEquals(color, updatedDogFromDb.color);
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