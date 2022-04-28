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
