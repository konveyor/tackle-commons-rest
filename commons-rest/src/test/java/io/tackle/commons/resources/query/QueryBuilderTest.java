package io.tackle.commons.resources.query;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.Test;

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueryBuilderTest {
    @Test
    public void test() {
        final List<Class<?>> results = new ArrayList<>();
        Arrays.stream(GoodEntity.class.getDeclaredFields())
                .forEach(field -> results.add(QueryBuilder.getType(field)));
        assertEquals(3, results.size());
        assertThat(results, IsIterableContainingInOrder.contains(String.class, Integer.class, Long.class));
        
        assertThrows(BadRequestException.class, () -> {
            Arrays.stream(BadEntity.class.getDeclaredFields())
                    .forEach(field -> results.add(QueryBuilder.getType(field)));
        });
    }
}
