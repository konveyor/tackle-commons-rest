package io.tackle.commons.testcontainers;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeycloakTestResourceTest {

    @Test
    public void testExternalUrl() {
        String keycloakUrl = "https://foo.test.com/sample";
        String quarkusAuthUrl = "quarkus.oidc.auth-server-url";
        try {
            withEnvironmentVariable("TACKLE_KEYCLOAK_TEST_URL", keycloakUrl).execute(() -> {
                KeycloakTestResource keycloakTestResource = new KeycloakTestResource();
                Map<String, String> config = keycloakTestResource.start();
                assertTrue(config.containsKey(quarkusAuthUrl));
                assertEquals(keycloakUrl, config.get(quarkusAuthUrl));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStop() {
        KeycloakTestResource keycloakTestResource = new KeycloakTestResource();
        keycloakTestResource.stop();
    }

    @Test
    public void testInit() {
        KeycloakTestResource keycloakTestResource = new KeycloakTestResource();
        keycloakTestResource.init(Collections.emptyMap());
    }

}
