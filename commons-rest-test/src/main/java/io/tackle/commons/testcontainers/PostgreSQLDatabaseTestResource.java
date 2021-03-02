package io.tackle.commons.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Collections;
import java.util.Map;

public class PostgreSQLDatabaseTestResource implements QuarkusTestResourceLifecycleManager {

    public static final String IMAGE_TAG = "tag";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String DB_NAME = "dbName";
    private String tag;
    private String user;
    private String password;
    private String dbName;
    private PostgreSQLContainer<?> postgreSQLContainer;

    @Override
    public void init(Map<String, String> initArgs) {
        tag = initArgs.getOrDefault(IMAGE_TAG, "10.6");
        user = initArgs.getOrDefault(USER, "test");
        password = initArgs.getOrDefault(PASSWORD, "test");
        dbName = initArgs.getOrDefault(DB_NAME, "test_db");
        postgreSQLContainer = new PostgreSQLContainer<>(String.format("postgres:%s", tag))
                .withDatabaseName(dbName)
                .withUsername(user)
                .withPassword(password);
    }

    @Override
    public Map<String, String> start() {
        postgreSQLContainer.start();
        return Collections.singletonMap("quarkus.datasource.jdbc.url", postgreSQLContainer.getJdbcUrl());
    }

    @Override
    public void stop() {
        if (postgreSQLContainer != null) postgreSQLContainer.close();
    }
}
