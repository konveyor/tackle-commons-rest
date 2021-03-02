package io.tackle.commons.testcontainers;

import org.junit.jupiter.api.Test;

public class PostgreSQLDatabaseTestResourceTest {

    @Test
    public void testStop() {
        PostgreSQLDatabaseTestResource postgreSQLDatabaseTestResource = new PostgreSQLDatabaseTestResource();
        postgreSQLDatabaseTestResource.stop();
    }

}
