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
)public class PersonListFilteredResourceTest extends SecuredResourceTest {

    @BeforeAll
    public static void init() {
        PATH = "/person";
    }

    @Test
    public void testPersonOrderedListEndpoint() {
        given()
            .accept(ContentType.JSON)
            .param("sort", "name")
            .when().get(PATH)
            .then()
                .statusCode(200)
                .body("", iterableWithSize(2),
                        "id", containsInRelativeOrder(2, 4),
                        "name", containsInRelativeOrder("c", "d")
                );
    }

}