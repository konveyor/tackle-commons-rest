package io.tackle.commons.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Collections;
import java.util.Map;

public class KeycloakTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String KEYCLOAK_IMAGE = "jboss/keycloak";
    public static final String TACKLE_KEYCLOAK_TEST_URL = "TACKLE_KEYCLOAK_TEST_URL";
    public static final String IMAGE_TAG = "tag";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String REALM_NAME = "realmName";
    public static final String IMPORT_REALM_JSON_PATH = "importRealmJsonPath";

    private String tag;
    private String realmName;
    public GenericContainer<?> keycloak;

    @Override
    public void init(Map<String, String> initArgs) {
        tag = initArgs.getOrDefault(IMAGE_TAG, "12.0.2");
        realmName = initArgs.getOrDefault(REALM_NAME, "master");
        keycloak = new GenericContainer<>(String.format("%s:%s", KEYCLOAK_IMAGE, tag))
                .withExposedPorts(8080, 8443)
                //this check was not reliable
//                    .waitingFor(Wait.forHttp("/auth/realms/master"))
                .waitingFor(Wait.forLogMessage(".* started in .*", 1))
                .withEnv("KEYCLOAK_USER", initArgs.getOrDefault(USER, "admin"))
                .withEnv("KEYCLOAK_PASSWORD", initArgs.getOrDefault(PASSWORD, "admin"))
                .withEnv("DB_VENDOR", "h2");
        if (initArgs.get(IMPORT_REALM_JSON_PATH) != null) {
            keycloak.withEnv("KEYCLOAK_IMPORT", "/tmp/import-realm.json")
                    .withClasspathResourceMapping(initArgs.get(IMPORT_REALM_JSON_PATH), "/tmp/import-realm.json", BindMode.READ_WRITE, SelinuxContext.SINGLE);
        }
    }

    @Override
    public Map<String, String> start() {
        // used System.out due to lack of logger in QuarkusTestResourceLifecycleManager as reported in
        // https://github.com/quarkusio/quarkus/blob/6cdd2078f1e99eddc4e739f28c7d7808ce8af12b/test-framework/common/src/main/java/io/quarkus/test/common/QuarkusTestResourceLifecycleManager.java#L20-L21
        final String keycloakExternalUrl = System.getenv(TACKLE_KEYCLOAK_TEST_URL);
        if (keycloakExternalUrl != null) {
            System.out.printf("Keycloak externally provided with %s=%s\n", TACKLE_KEYCLOAK_TEST_URL, keycloakExternalUrl);
            return Collections.singletonMap("quarkus.oidc.auth-server-url", keycloakExternalUrl);
        }
        System.out.printf("[INFO] %s:%s starting...\n", KEYCLOAK_IMAGE, tag);
        keycloak.start();
        System.out.printf("[INFO] %s:%s started\n", KEYCLOAK_IMAGE, tag);
        return Collections.singletonMap(
                "quarkus.oidc.auth-server-url", String.format("https://localhost:%s/auth/realms/%s", keycloak.getMappedPort(8443), realmName)
        );
    }

    @Override
    public void stop() {
        if (keycloak != null) keycloak.stop();
    }
}
