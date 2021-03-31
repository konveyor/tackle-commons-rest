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
        return type.cast(generators.getOrDefault(type, value -> {throw new BadRequestException("Malformed filter value");}).apply(source));
    }
}
