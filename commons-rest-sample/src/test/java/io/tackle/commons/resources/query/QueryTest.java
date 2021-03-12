package io.tackle.commons.resources.query;

import io.tackle.commons.sample.entities.Person;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryTest {
    
    @Test
    public void testQuery() {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.addAll("cani.name", "a", "b");
        headers.addAll("cats.name", "a", "b");
        headers.addAll("fishes.foo", "a", "b");
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getQueryParameters(true)).thenReturn(headers);
        Query query = QueryBuilder.withPanacheEntity(Person.class).andUriInfo(uriInfo).build();
        // "SELECT table FROM Person table JOIN table.fishes fishes JOIN table.cats cats JOIN table.dogs cani WHERE ( lower(fishes.foo) LIKE lower(:fishes_foo201) or lower(fishes.foo) LIKE lower(:fishes_foo215) ) and ( lower(cats.name) LIKE lower(:cats_name562) or lower(cats.name) LIKE lower(:cats_name71) ) and ( lower(cani.name) LIKE lower(:cani_name284) or lower(cani.name) LIKE lower(:cani_name841) )"
        assertTrue(query.getQuery().startsWith("SELECT table FROM Person table JOIN table.fishes fishes JOIN table.cats cats JOIN table.dogs cani WHERE "));
    }
}
