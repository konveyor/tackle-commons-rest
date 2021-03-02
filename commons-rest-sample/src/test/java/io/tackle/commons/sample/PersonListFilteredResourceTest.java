package io.tackle.commons.sample;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.tackle.commons.tests.SecuredResourceTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInRelativeOrder;

@QuarkusTest
@Disabled
public class PersonListFilteredResourceTest extends SecuredResourceTest {

    @BeforeAll
    public static void init() {
        PATH = "/person";
    }

    @Test
    public void testPersonOrderedListEndpoint() {
        given()
            .accept(ContentType.JSON)
            .param("sort", "name")
            .when().get()
            .then()
                .statusCode(200)
                .body("id", containsInRelativeOrder(1),
                        "name", containsInRelativeOrder("c")
                );
    }

}