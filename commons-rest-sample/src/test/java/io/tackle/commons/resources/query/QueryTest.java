/*
 * Copyright © 2021 Konveyor (https://konveyor.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public void testQueryBuilderWithUriInfo() {
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.addAll("cani.name", "a", "b");
        queryParameters.addAll("cats.name", "a", "b");
        queryParameters.addAll("fishes.foo", "a", "b");
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getQueryParameters(true)).thenReturn(queryParameters);
        Query query = QueryBuilder.withPanacheEntity(Person.class).andUriInfo(uriInfo).build();
        // "SELECT table FROM Person table JOIN table.fishes fishes JOIN table.cats cats JOIN table.dogs cani WHERE ( lower(fishes.foo) LIKE lower(:fishes_foo201) or lower(fishes.foo) LIKE lower(:fishes_foo215) ) and ( lower(cats.name) LIKE lower(:cats_name562) or lower(cats.name) LIKE lower(:cats_name71) ) and ( lower(cani.name) LIKE lower(:cani_name284) or lower(cani.name) LIKE lower(:cani_name841) )"
        assertTrue(query.getQuery().startsWith("SELECT table FROM Person table JOIN table.fishes fishes JOIN table.cats cats JOIN table.dogs cani WHERE "));
    }

    @Test
    public void testQueryBuilderWithMultivaluedMap() {
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.addAll("cani.name", "a", "b");
        queryParameters.addAll("cats.name", "a", "b");
        queryParameters.addAll("fishes.foo", "a", "b");
        Query query = QueryBuilder.withPanacheEntity(Person.class).andMultivaluedMap(queryParameters).build();
        // "SELECT table FROM Person table JOIN table.fishes fishes JOIN table.cats cats JOIN table.dogs cani WHERE ( lower(fishes.foo) LIKE lower(:fishes_foo201) or lower(fishes.foo) LIKE lower(:fishes_foo215) ) and ( lower(cats.name) LIKE lower(:cats_name562) or lower(cats.name) LIKE lower(:cats_name71) ) and ( lower(cani.name) LIKE lower(:cani_name284) or lower(cani.name) LIKE lower(:cani_name841) )"
        assertTrue(query.getQuery().startsWith("SELECT table FROM Person table JOIN table.fishes fishes JOIN table.cats cats JOIN table.dogs cani WHERE "));
    }
}
