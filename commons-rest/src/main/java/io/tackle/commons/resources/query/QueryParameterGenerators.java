/*
 * Copyright © 2021 the Konveyor Contributors (https://konveyor.io/)
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

import javax.ws.rs.BadRequestException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Based on the typesafe heterogeneous container (item#33)
 * with the addition of {@link Function} management
 * for typesafe heterogeneous values generation.
 */
public class QueryParameterGenerators {
    private final Map<Class<?>, Function<String, ?>> generators = new HashMap<>();
    
    public <TYPE> void putQueryParameterGenerator(Class<TYPE> type, Function<String, TYPE> generator) {
        generators.put(Objects.requireNonNull(type), generator);
    }

    public <TYPE> TYPE getQueryParameterValue(Class<TYPE> type, String source) {
        try {
            return type.cast(generators.getOrDefault(type, value -> {
                throw new BadRequestException("Malformed filter value");
            }).apply(source));
        } catch (NumberFormatException nfe) {
            throw new BadRequestException("Malformed filter value");
        }
    }
}
