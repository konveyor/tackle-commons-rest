package io.tackle.commons.resources.query;

import org.junit.jupiter.api.Test;

import javax.ws.rs.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueryParameterGeneratorsTest {
    
    @Test
    public void test() {
        final QueryParameterGenerators generators = new QueryParameterGenerators();
        generators.putQueryParameterGenerator(Long.class, value -> {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException e) {
                throw new BadRequestException("Malformed filter value");
            }
        });
        generators.putQueryParameterGenerator(String.class, value -> value);
        assertEquals(42L, generators.getQueryParameterValue(Long.class, "42"));
        assertEquals("foo", generators.getQueryParameterValue(String.class, "foo"));
        assertThrows(BadRequestException.class, () -> {
            generators.getQueryParameterValue(Long.class, "foo");
        });
        assertThrows(BadRequestException.class, () -> {
            generators.getQueryParameterValue(Integer.class, "42");
        });
    }
}
