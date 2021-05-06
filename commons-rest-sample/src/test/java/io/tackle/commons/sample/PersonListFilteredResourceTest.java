package io.tackle.commons.sample;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.tackle.commons.sample.entities.Dog;
import io.tackle.commons.sample.entities.Person;
import io.tackle.commons.testcontainers.KeycloakTestResource;
import io.tackle.commons.testcontainers.PostgreSQLDatabaseTestResource;
import io.tackle.commons.tests.SecuredResourceTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;

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
                @ResourceArg(name = KeycloakTestResource.REALM_NAME, value = "quarkus"),
                // Added for testing that forcing a specific image tag for keycloak works
                // If needed, in the future, update it to a later fixed version
                @ResourceArg(name = KeycloakTestResource.IMAGE_TAG, value = "12.0.3")
        }
)public class PersonListFilteredResourceTest extends SecuredResourceTest {

    @BeforeAll
    public static void init() {
        PATH = "/person";
    }

    @Test
    public void testPersonOrderedListEndpoint() {
        given()
            .accept(ContentType.JSON)
            .param("sort", "-dogs.size,name")
            .when().get(PATH)
            .then()
            .statusCode(200)
            .body("", iterableWithSize(3),
                    "id", containsInRelativeOrder(2, 4, 8),
                    "name", containsInRelativeOrder("c", "d", "m")
            );
    }

    @Test
    public void testFilteredByOneToManyFieldListsHalEndpoint() {
        given()
                .accept("application/hal+json")
                .queryParam("sort", "-id,books.size")
                .queryParam("cani.name", "E")
                .queryParam("cani.name", "G")
                .when().get(PATH)
                .then()
                .statusCode(200)
                .header("Link", is("<http://localhost:8081/person?page=0&size=20&sort=-id%2Cbooks.size&cani.name=E&cani.name=G>; rel=\"last\""))
                .body("_embedded.person.size()", is(2),
                        "total_count", is(2));
    }

    @Test
    public void testFilteredByManyToManyFieldListsHalEndpoint() {
        given()
                .accept("application/hal+json")
                .queryParam("sort", "-id,horses.size()")
                .queryParam("books.title", "o")
                .queryParam("books.title", "another")
                .when().get(PATH)
                .then()
                .log().all()
                .statusCode(200)
                .header("Link", is("<http://localhost:8081/person?page=0&size=20&sort=-id%2Chorses.size%28%29&books.title=o&books.title=another>; rel=\"last\""))
                .body("_embedded.person.size()", is(2),
                        "_embedded.person.id", containsInRelativeOrder(8, 2),
                        "_embedded.person[1].books", iterableWithSize(2),
                        "_embedded.person[1].books.id", containsInRelativeOrder(9, 10),
                        "_embedded.person[1].books.title", containsInRelativeOrder("n", "o"),
                        "total_count", is(2));
    }

    @Test
    // https://github.com/konveyor/tackle-commons-rest/issues/48
    public void testFilteredByIdListsHalEndpoint() {
        given()
                .accept("application/hal+json")
                .queryParam("sort", "-id,horses.size()")
                .queryParam("id", "4")
                .queryParam("id", "8")
                .when()
                .get(PATH)
                .then()
                .log().body()
                .statusCode(200)
                .header("Link", is("<http://localhost:8081/person?page=0&size=20&sort=-id%2Chorses.size%28%29&id=4&id=8>; rel=\"last\""))
                .body("_embedded.person.size()", is(2),
                        "_embedded.person.id", containsInRelativeOrder(8, 4),
                        "_embedded.person[0].books", iterableWithSize(1),
                        "_embedded.person[0].books[0].id", is(10),
                        "_embedded.person[1].dogs", iterableWithSize(1),
                        "total_count", is(2));
    }

    @Test
    // https://github.com/konveyor/tackle-commons-rest/issues/47
    public void testFilteringTheSamePersonByMultipleBooks() {
        given()
                .accept("application/hal+json")
                .queryParam("sort", "-name")
                .queryParam("books.title", "n")
                .queryParam("books.title", "o")
                .when()
                .get(PATH)
                .then()
                .log().body()
                .statusCode(200)
                .body("_embedded.person.size()", is(2),
                        "_embedded.person.id", containsInRelativeOrder(8, 2),
                        "_embedded.person[1].books.size()", is(2),
                        "_embedded.person[0]._links.size()", is(5),
                        "_embedded.person[0]._links.self.href", is("http://localhost:8081/person/8"),
                        "_links.size()", is(4));
    }

    @Test
    // https://github.com/konveyor/tackle-commons-rest/issues/53
    public void testSortByCollectionSizeWithDeletedEntities() {
        // initial situation:
        // person #2 has 2 dogs
        // person #4 has 1 dog
        // person #8 has no dogs
        given()
                .accept("application/hal+json")
                .queryParam("sort", "-dogs.size")
                .when()
                .get(PATH)
                .then()
                .statusCode(200)
                .body("_embedded.person.size()", is(3),
                        "_embedded.person.id", containsInRelativeOrder(2, 4, 8),
                        "_embedded.person[0].dogs.size()", is(2),
                        "_embedded.person[1].dogs.size()", is(1),
                        "_embedded.person[2].dogs.size()", is(0));

        // add 2 more dogs to person #4
        Person d = new Person();
        d.id = 4L;
        Dog foo = new Dog();
        foo.name = "foo";
        foo.owner = d;
        foo.id = Long.valueOf(given()
                .contentType("application/json")
                .body(foo)
                .when()
                .post("/dog")
                .then()
                .statusCode(201)
                .extract()
                .path("id")
                .toString());

        Dog bar = new Dog();
        bar.name = "bar";
        bar.owner = d;
        bar.id = Long.valueOf(given()
                .contentType("application/json")
                .body(bar)
                .when()
                .post("/dog")
                .then()
                .statusCode(201)
                .extract()
                .path("id")
                .toString());

        // check the initial situation has changed accordingly to latest changes:
        // person #4 has 3 dogs
        // person #2 has 2 dogs
        // person #8 has no dogs
        given()
                .accept("application/hal+json")
                .queryParam("sort", "-dogs.size")
                .when()
                .get(PATH)
                .then()
                .statusCode(200)
                .body("_embedded.person.size()", is(3),
                       "_embedded.person.id", containsInRelativeOrder(4, 2, 8),
                       "_embedded.person[0].dogs.size()", is(3),
                       "_embedded.person[1].dogs.size()", is(2),
                       "_embedded.person[2].dogs.size()", is(0));

        // now delete the 2 dogs just created
        given()
                .contentType("application/json")
                .pathParam("id", foo.id)
                .when()
                .delete("/dog/{id}")
                .then()
                .statusCode(204);

        given()
                .contentType("application/json")
                .pathParam("id", bar.id)
                .when()
                .delete("/dog/{id}")
                .then()
                .statusCode(204);

        // check the initial situation has been restored:
        // person #2 has 2 dogs
        // person #4 has 1 dog
        // person #8 has no dogs
        given()
                .accept("application/hal+json")
                .queryParam("sort", "-dogs.size()")
                .when()
                .get(PATH)
                .then()
                .statusCode(200)
                .body("_embedded.person.size()", is(3),
                        "_embedded.person.id", containsInRelativeOrder(2, 4, 8),
                        "_embedded.person[0].dogs.size()", is(2),
                        "_embedded.person[1].dogs.size()", is(1),
                        "_embedded.person[2].dogs.size()", is(0));
    }
}
