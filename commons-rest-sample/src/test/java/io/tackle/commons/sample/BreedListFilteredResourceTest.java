package io.tackle.commons.sample;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.tackle.commons.testcontainers.KeycloakTestResource;
import io.tackle.commons.testcontainers.PostgreSQLDatabaseTestResource;
import io.tackle.commons.tests.SecuredResourceTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInRelativeOrder;
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
)public class BreedListFilteredResourceTest extends SecuredResourceTest {

    @BeforeAll
    public static void init() {
        PATH = "/breed";
    }

    @Test
    public void testOrderedListEndpoint() {
        given()
            .accept(ContentType.JSON)
            .param("sort", "name")
            .when().get(PATH)
            .then()
            .statusCode(200)
            .body("", iterableWithSize(3),
                    "id", containsInRelativeOrder(1, 12, 13),
                    "name", containsInRelativeOrder("i", "r", "s")
            );
    }

    @Test
    public void testFilterWithFilterableEqualListEndpoint() {
        given()
                .accept(ContentType.JSON)
                .param("sort", "name")
                .param("origin", "1")
                .param("internationalId", "456")
                .when()
                .get(PATH)
                .then()
                .statusCode(200)
                .body("", iterableWithSize(1),
                        "id", containsInRelativeOrder(12),
                        "name", containsInRelativeOrder("r"),
                        "origin", containsInRelativeOrder("1")
                );

        given()
                .accept(ContentType.JSON)
                .param("sort", "name")
                .param("internationalId", "foo")
                .when()
                .get(PATH)
                .then()
                .statusCode(400);
    }

    @Test
    public void testFilterWithFilterableEqualOnElementCollectionListEndpoint() {
        given()
                .accept(ContentType.JSON)
                .param("sort", "-id")
                .param("translations.translations", "u")
                .when()
                .get(PATH)
                .then()
                .statusCode(200)
                .body("", iterableWithSize(1),
                        "id", containsInRelativeOrder(1),
                        "name", containsInRelativeOrder("i"),
                        "origin", containsInRelativeOrder("j")
                );

        given()
                .accept(ContentType.JSON)
                .param("sort", "-origin")
                .param("translations.translations", "v")
                .when()
                .get(PATH)
                .then()
                .statusCode(200)
                .body("", iterableWithSize(3),
                        "id", containsInRelativeOrder(1, 13, 12),
                        "name", containsInRelativeOrder("i", "s", "r"),
                        "origin", containsInRelativeOrder("j", "11", "1")
                );
    }
}
