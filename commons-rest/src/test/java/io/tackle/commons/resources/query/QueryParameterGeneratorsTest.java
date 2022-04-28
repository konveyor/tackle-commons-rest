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
